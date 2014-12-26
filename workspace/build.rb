#!/usr/bin/env ruby

require 'js_base'
require 'trollop'

class ProgramException < Exception; end

class Build

  def run(argv)
    begin
      @options = parse_arguments(argv)
      @verbose = @options[:verbose]
      @project_names = @options[:leftovers]
      if @project_names.empty?
        raise ProgramException,"No projects specified"
      end

      @count = 0

      @project_names.each{|x| build_project(x)}
    rescue ProgramException => e
      puts "*** Aborted!  #{e.message}"
      exit 1
    end
  end

  def build_project(project_path)
    puts "Building project #{project_path}" if @verbose
    @src_path = File.join(project_path,"src")
    @bin_path = File.join(project_path,"bin")
    if @options[:clean]
      FileUtils.rm_rf(@bin_path)
    end
    Dir.mkdir(@bin_path) if !File.directory?(@bin_path)
    build_project_aux(@src_path,@bin_path)
  end

  def build_project_aux(src_path,bin_path)
    entries = FileUtils.directory_entries(src_path)
    entries.each do |basename|
      break if @count > 5
      file = File.join(src_path,basename)
      if File.directory?(file)
        build_project_aux(file,File.join(bin_path,basename))
      else
        next if !basename.end_with?('.java')

        src_file = file
        # Determine if class file exists and is not older than source
        class_file = FileUtils.change_extension(File.join(bin_path,basename),"class")
        if File.file?(class_file) && File.mtime(class_file) >= File.mtime(src_file)
          puts "   (not recompiling valid class file #{class_file})" if @verbose
          next
        end

        cmd = "javac"
        cmd << " -cp libs/java-json.jar:#{@bin_path}"
        cmd << " -sourcepath #{@src_path}"
        cmd << " -d #{@bin_path} #{src_file}"

        puts "Recompiling #{src_file}" if @verbose

        scall(cmd)
        @count += 1
      end
    end

  end

  def parse_arguments(argv)
      parser = Trollop::Parser.new do
          banner <<-EOS
Compiles Java projects
      EOS
          opt :clean, "clean old class files"
          opt :verbose, "display progress"
      end
      options = Trollop::with_standard_exception_handling parser do
        parser.parse argv
      end
      options[:leftovers] = parser.leftovers || []
      options
    end

end

if __FILE__ == $0
  Build.new.run(ARGV)
end
