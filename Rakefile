require 'rake/clean'

HERE = File.expand_path(File.dirname(__FILE__))

SUPPORT_DIR = File.join(HERE,"support")
ZINC_HOME   = File.join(SUPPORT_DIR,"zinc")
ZINC        = File.join(ZINC_HOME,"bin","zinc")
SCALA_HOME  = File.join(SUPPORT_DIR,"scala")
SCALA       = File.join(SCALA_HOME,"bin","scala")
OUTPUT_DIR  = File.join(HERE,"build","classes")

CLEAN << OUTPUT_DIR

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

directory OUTPUT_DIR

desc "Run the app"
task :run => :compile do
  sh "#{SCALA} -cp #{OUTPUT_DIR} main"
end

file SCALA_HOME do
  candidates = Dir["#{SUPPORT_DIR}/scala-2.10.0*"]
  if candidates.size == 1
    ln_s candidates[0],SCALA_HOME
  else
    raise "Don't know how to handle #{candidates.size} scala installs"
  end
end

file ZINC_HOME do
  candidates = Dir["#{SUPPORT_DIR}/zinc-*"]
  if candidates.size == 1
    ln_s candidates[0],ZINC_HOME
  else
    raise "Don't know how to handle #{candidates.size} scala installs"
  end
end

desc "Setup symlinks to support commands"
task "support:setup" => [SCALA_HOME,ZINC_HOME] do
end
