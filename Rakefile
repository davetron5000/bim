require 'rake/clean'

HERE = File.expand_path(File.dirname(__FILE__))

require File.join(HERE,'dependencies.rb')

SUPPORT_DIR = File.join(HERE,"support")
JAR_DIR     = File.join(SUPPORT_DIR,"jars")
ZINC_HOME   = File.join(SUPPORT_DIR,"zinc")
ZINC        = File.join(ZINC_HOME,"bin","zinc")
SCALA_HOME  = File.join(SUPPORT_DIR,"scala")
SCALA       = File.join(SCALA_HOME,"bin","scala")
OUTPUT_DIR  = File.join(HERE,"build","classes")

CLEAN << OUTPUT_DIR

directory OUTPUT_DIR
directory SUPPORT_DIR
directory JAR_DIR

def zinc(*args)
  sh "#{ZINC} -nailed -scala-home #{SCALA_HOME} -no-color #{args.join(' ')}"
end

def nailed?
  `#{ZINC} -status`
  $?.success?
end

task :nailed do
  if nailed?
    puts "Nailgun compiler running"
  else
    puts "Nailgun compiler not running"
  end
end

desc "Stop the nailgun compiler"
task "nailed:shutdown" do
  zinc "-shutdown"
end

SOURCES = Dir["src/scala/**/*.scala"]

desc "Compile out of date Scala classes"
task :compile => ["support:setup",OUTPUT_DIR] do
  zinc "-d",OUTPUT_DIR,SOURCES
end


desc "Run the app"
task :run => :compile do
  sh "#{SCALA} -cp #{OUTPUT_DIR} main"
end

def support_link(pattern,destination)
  candidates = Dir["#{SUPPORT_DIR}/#{pattern}*"]
  if candidates.size == 1
    ln_s candidates[0],destination
  else
    raise "Don't know how to handle #{candidates.size} scala installs"
  end
end

def setup_unpackable_dep(name,url,dir)
  archive = File.join(dir,url.split(/\//).last)
  parts = archive.split(/\./)
  filename = parts[0..-2].join(".")
  extension = parts.last

  file filename => dir do
    sh "curl #{url} > #{archive}"
    chdir dir do
      case extension.downcase
      when "tgz"
        sh "tar xvfz #{archive}" do |results,status|
          if status.success?
            rm archive
          else
            raise "Problem un-taring #{archive}"
          end
        end
      else
        raise "Don't know how to handle #{extension} files"
      end
    end
  end
  task "dependency:#{name}" => filename
end

def setup_regular_dep(name,url,dir)
  jar_file = File.join(dir,url.split(/\//).last)

  file jar_file => dir do
    sh "curl #{url} > #{jar_file}"
  end
  task "dependency:#{name}" => jar_file
end


DEPENDENCIES.each do |name,payload|
  url,unpack = if payload.kind_of?(Hash)
                 [payload[:url],payload[:unpack]]
               else
                 [payload,false]
               end
  if unpack
    setup_unpackable_dep(name,url,SUPPORT_DIR)
  else
    setup_regular_dep(name,url,JAR_DIR)
  end
end

file SCALA_HOME => "dependency:scala" do
  support_link "scala-2.10.0",SCALA_HOME
end
file ZINC_HOME => "dependency:zinc" do
  support_link "zinc-",ZINC_HOME
end

desc "Setup symlinks to support commands"
task "support:setup" => ([SCALA_HOME,ZINC_HOME] + DEPENDENCIES.keys.map { |_| "dependency:#{_}" })
