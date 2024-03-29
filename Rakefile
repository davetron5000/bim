require 'rake/clean'

HERE = File.expand_path(File.dirname(__FILE__))

$: << File.join(HERE,"src","rake")

SUPPORT_DIR       = File.join(HERE,"support")
JAR_DIR           = File.join(SUPPORT_DIR,"jars")
ZINC_HOME         = File.join(SUPPORT_DIR,"zinc")
ZINC              = File.join(ZINC_HOME,"bin","zinc")
SCALA_HOME        = File.join(SUPPORT_DIR,"scala")
SCALA             = File.join(SCALA_HOME,"bin","scala")
BUILD_DIR         = File.join(HERE,"build")
OUTPUT_DIR        = File.join(BUILD_DIR,"classes")
TEST_OUTPUT_DIR   = File.join(BUILD_DIR,"test","classes")
SOURCES           = Dir["src/scala/**/*.scala"]
UNIT_TEST_SOURCES = Dir["test/unit/scala/**/*.scala"]
PERF_TEST_SOURCES = Dir["test/performance/*/test.rb"]
VERBOSE           = ENV["VERBOSE"] || false
ZINC_LOG_LEVEL    = VERBOSE ? "info" : "warn"

require File.join(HERE,'dependencies.rb')
require 'dependency_management'
require 'performance_test'
JARS = setup_dependency_tasks(DEPENDENCIES,SUPPORT_DIR,JAR_DIR)

CLEAN << BUILD_DIR

directory OUTPUT_DIR
directory TEST_OUTPUT_DIR
directory SUPPORT_DIR
directory JAR_DIR

def zinc(*args)
  sh "#{ZINC} -idle-timeout 1h -log-level #{ZINC_LOG_LEVEL} -S-feature -S-deprecation -nailed -scala-home #{SCALA_HOME} -no-color #{args.join(' ')}", :verbose => VERBOSE
end

task :nailed do
  `#{ZINC} -status`
  if $?.success?
    puts "Nailgun compiler running"
  else
    puts "Nailgun compiler not running"
  end
end

desc "Stop the nailgun compiler"
task "nailed:shutdown" do
  zinc "-shutdown"
end


desc "Compile out of date Scala classes"
task :compile => ["support:setup",OUTPUT_DIR] do
  zinc "-d",OUTPUT_DIR,SOURCES
end

task :test => ['test:unit','test:performance']

desc "Run unit tests"
task 'test:unit' => :compile do
  zinc "-cp",(JARS + [OUTPUT_DIR]).join(":"),"-d",TEST_OUTPUT_DIR,UNIT_TEST_SOURCES
  test_classes = Dir["#{TEST_OUTPUT_DIR}/**/*.class"].map { |classfile|
    classfile.gsub(/^#{TEST_OUTPUT_DIR}\//,'').gsub(/.class$/,'').gsub(/\//,'.')
  }.select { |_| _ =~ /Test$/ }
  sh "#{SCALA} -cp #{(JARS + [OUTPUT_DIR,TEST_OUTPUT_DIR]).join(':')} org.junit.runner.JUnitCore #{test_classes.join(' ')}", :verbose => VERBOSE
end

desc 'Run performance tests'
task 'test:performance' => :compile do
  printf("%-40s|%12s|%12s|\n","TEST","SERIAL","PARALLEL")
  puts "-" * (40 + 12 + 12 + 3)
  tests = PERF_TEST_SOURCES.each do |test_file|
    single,threaded,error = run_performance_test(test_file)
    desc = @config[:description] || test_file
    shortened_desc = desc.length <= 40 ? desc : desc[-40..-1]
    printf("%40s|%10.2fms|%10.2fms|%s\n",shortened_desc,single || 0,threaded || 0,error && error.message)
  end
end


def link_version_to_generic(support_dir,pattern,destination)
  candidates = Dir["#{support_dir}/#{pattern}*"]
  candidates = candidates.reject {|_| _ =~ /-RC\d+$/ } if candidates.size > 1
  candidates = candidates.sort { |a,b|
    a_version = if a =~ /^.*-(\d+\.\d+\.\d+)$/
                  $1
                else
                  "0.0.0"
                end
    b_version = if b =~ /^.*-(\d+\.\d+\.\d+)$/
                  $1
                else
                  "0.0.0"
                end
    a_major,a_minor,a_patch = a_version.split(".")
    b_major,b_minor,b_patch = a_version.split(".")

    if a_major == b_major
      if a_minor == b_minor
        a_patch <=> b_patch
      else
        a_minor <=> b_minor
      end
    else
      a_major <=> b_major
    end
  }
  candidate = candidates.last
  raise "No #{pattern} installs faound!" if candidate.nil?
  if candidates.size > 1
    puts "Found two installs for #{pattern}, using #{candidate}"
  end
  ln_s candidate,destination
end

file SCALA_HOME => "dependency:scala" do
  link_version_to_generic SUPPORT_DIR,"scala-2.10",SCALA_HOME
end
file ZINC_HOME => "dependency:zinc" do
  link_version_to_generic SUPPORT_DIR,"zinc-",ZINC_HOME
end

desc "Setup symlinks to support commands"
task "support:setup" => ([SCALA_HOME,ZINC_HOME] + DEPENDENCIES.keys.map { |_| "dependency:#{_}" })

desc "run an app"
task :run, [:klass] => [:compile] do |t, args|
  klass = args.klass
  raise "Need a class" if klass.nil?
  sh "#{SCALA} -cp #{(JARS + [OUTPUT_DIR]).join(':')} #{klass}"
end
task :default => :test
