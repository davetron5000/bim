require 'rake/clean'

HERE = File.expand_path(File.dirname(__FILE__))

$: << File.join(HERE,"src","rake")

SUPPORT_DIR     = File.join(HERE,"support")
JAR_DIR         = File.join(SUPPORT_DIR,"jars")
ZINC_HOME       = File.join(SUPPORT_DIR,"zinc")
ZINC            = File.join(ZINC_HOME,"bin","zinc")
SCALA_HOME      = File.join(SUPPORT_DIR,"scala")
SCALA           = File.join(SCALA_HOME,"bin","scala")
OUTPUT_DIR      = File.join(HERE,"build","classes")
TEST_OUTPUT_DIR = File.join(HERE,"build","test","classes")
SOURCES         = Dir["src/scala/**/*.scala"]
TEST_SOURCES    = Dir["test/scala/**/*.scala"]
VERBOSE         = ENV["VERBOSE"] || false
ZINC_LOG_LEVEL  = VERBOSE ? "info" : "warn"

require File.join(HERE,'dependencies.rb')
require 'dependency_management'
JARS = setup_dependency_tasks(DEPENDENCIES,SUPPORT_DIR,JAR_DIR)

CLEAN << OUTPUT_DIR

directory OUTPUT_DIR
directory TEST_OUTPUT_DIR
directory SUPPORT_DIR
directory JAR_DIR

def zinc(*args)
  sh "#{ZINC} -log-level #{ZINC_LOG_LEVEL} -S-deprecation -nailed -scala-home #{SCALA_HOME} -no-color #{args.join(' ')}", :verbose => VERBOSE
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

desc "Run tests"
task :test => :compile do
  zinc "-cp",(JARS + [OUTPUT_DIR]).join(":"),"-d",TEST_OUTPUT_DIR,TEST_SOURCES
  test_classes = Dir["#{TEST_OUTPUT_DIR}/**/*.class"].map { |classfile|
    classfile.gsub(/^#{TEST_OUTPUT_DIR}\//,'').gsub(/.class$/,'').gsub(/\//,'.')
  }
  sh "#{SCALA} -cp #{(JARS + [OUTPUT_DIR,TEST_OUTPUT_DIR]).join(':')} org.junit.runner.JUnitCore #{test_classes.join(' ')}", :verbose => VERBOSE
end


desc "Run the app"
task :run => :compile do
  sh "#{SCALA} -cp #{OUTPUT_DIR} main"
end

def link_version_to_generic(support_dir,pattern,destination)
  candidates = Dir["#{support_dir}/#{pattern}*"]
  if candidates.size == 1
    ln_s candidates[0],destination
  else
    raise "Don't know how to handle #{candidates.size} scala installs"
  end
end

file SCALA_HOME => "dependency:scala" do
  link_version_to_generic SUPPORT_DIR,"scala-2.10.0",SCALA_HOME
end
file ZINC_HOME => "dependency:zinc" do
  link_version_to_generic SUPPORT_DIR,"zinc-",ZINC_HOME
end

desc "Setup symlinks to support commands"
task "support:setup" => ([SCALA_HOME,ZINC_HOME] + DEPENDENCIES.keys.map { |_| "dependency:#{_}" })
