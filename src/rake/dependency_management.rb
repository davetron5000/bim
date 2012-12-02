def setup_unpackable_dep(name,url,dir)
  archive = File.join(dir,url.split(/\//).last)
  parts = archive.split(/\./)
  filename = parts[0..-2].join(".")
  extension = parts.last

  file filename => dir do
    sh "curl -L #{url} > #{archive}"
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
  desc "Download and install #{name} if needed"
  task "dependency:#{name}" => filename
end

def setup_regular_dep(name,url,dir)
  jar_file = File.join(dir,url.split(/\//).last)

  file jar_file => dir do
    sh "curl #{url} > #{jar_file}"
  end
  desc "Download and install #{name} if needed"
  task "dependency:#{name}" => jar_file
  jar_file
end

def setup_dependency_tasks(dependencies,support_dir,jar_dir)
  jars = []
  dependencies.each do |name,payload|
    url,unpack = if payload.kind_of?(Hash)
                   [payload[:url],payload[:unpack]]
                 else
                   [payload,false]
                 end
    if unpack
      setup_unpackable_dep(name,url,support_dir)
    else
      jars << setup_regular_dep(name,url,jar_dir)
    end
  end
  jars
end

