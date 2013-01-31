require 'benchmark'
require 'thread'
require 'net/http'

def run_performance_test(test_file)
  @config = nil
  load test_file
  raise "#{test_file} didn't set @config!" if @config.nil?
  args = Array(@config.fetch(:args)).join(' ')
  pid = Process.spawn("#{SCALA} -cp #{(JARS + [OUTPUT_DIR]).join(':')} #{@config.fetch(:app)} #{args} > /dev/null 2>&1", :pgroup => true)
  process_group_id = Process.getpgid(pid)
  sleep 2 # allow startup
  begin
    single   = PerformanceTest.run_one_performance_test(@config)
    threaded = PerformanceTest.run_one_performance_test(@config, :threaded => true)
    [single,threaded]
  rescue => ex
    [single,threaded,ex]
  ensure
    Process.kill("TERM",-1 * process_group_id)
  end
end

module PerformanceTest
  def self.run_one_performance_test(config,options = {})
    measures = []
    threads  = []
    fetcher  = options[:threaded] ? :fetch_with_curl : :fetch_with_net_http
    m        = mutex_if_needed(options[:threaded])

    100.times do |i|
      t = in_thread_if_needed(options[:threaded]) do 
        config[:urls].each do |url,expected_status|
          measure = Benchmark.realtime {
            assert_code(expected_status,send(fetcher,url))
          }
          m.synchronize { measures << measure }
        end
      end
      m.synchronize { threads << t }
    end
    threads.each { |t| t.join }

    measures.reduce(&:+) / measures.size * 1000
  end

  class NullMutex
    def synchronize(&block)
      block.call
    end
  end

  def self.mutex_if_needed(use_thread)
    use_thread ? Mutex.new : NullMutex.new
  end

  def self.in_thread_if_needed(use_thread,&block)
    if use_thread
      Thread.new {
        block.call
      }
    else
      block.call
      OpenStruct.new(:join => true)
    end
  end

  def self.fetch_with_curl(url)
    code = `curl -sL -w "%{http_code}" "#{url}" -o /dev/null`
  end

  def self.fetch_with_net_http(url)
    response = Net::HTTP.get_response(URI.parse(url))
    response.code
  end

  def self.assert_code(expected_status,code)
    if code != expected_status.to_s
      raise "Expected #{expected_status}, got #{response.code}\n#{response.inspect}"
    end
  end
end
