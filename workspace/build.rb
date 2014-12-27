#!/usr/bin/env ruby

require 'js_base'
require 'trollop'

JOGAMP = "/Users/home/jogamp"

class ProgramException < Exception; end

# Project information.  Name, dependent projects, required libraries
#
class Project
  @@map = Hash.new

  def self.map
    @@map
  end

  attr_accessor :name,:projects,:libraries

  def initialize(name)
    @name = name
    @projects = []
    @libraries = []
  end

  def add_libraries(libraries)
    @libraries.concat(libraries)
    self
  end

  def add_projects(projects)
    @projects.concat(projects)
    self
  end

  def src_path
    "#{name}/src"
  end

  def bin_path
    "#{name}/bin"
  end

  def verify_dependent_projects_exist
    @projects.each do |proj|
      die "No project '#{proj}' has been defined" if !@@map.has_key?(proj)
    end
  end

  def self.add(name)
    die "Project '#{name}' already exists" if @@map.has_key?(name)
    p = Project.new(name)
    @@map[p.name] = p
    p
  end

end


# The main app
#
class App

  def run(argv)
    begin

      @options = parse_arguments(argv)
      @verbose = @options[:verbose]
      raise ProgramException,"Extra arguments" if !@options[:leftovers].empty?

      # Declare the various projects and their dependencies
      #

      Project.add("basic").add_libraries %w(libs/java-json.jar)

      # This project is deprecated:
      Project.add("base").add_projects %w(basic)

      Project.add("apputil")
      .add_projects(%w(base basic scanning streams))
      .add_libraries(%w(/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/ui.jar))
      Project.add("images").add_projects(%w(base basic streams))
      .add_libraries(["/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar"])
      Project.add("scanning").add_projects %w(base basic streams)
      Project.add("streams").add_projects %w(base basic)
      Project.add("myopengl").add_projects %w(base basic jogl tex)
      Project.add("tex").add_projects %w(apputil base basic images scanning streams)
      Project.add("jogl")
      .add_libraries(["#{JOGAMP}/gluegen/build/gluegen-rt.jar","#{JOGAMP}/jogl/build/jar/jogl-all.jar"])

      Project.add("testUtils")
      .add_libraries(["../eclipse_config/junit.jar"])

      Project.add("basicTest")
      .add_projects(["basic"])

      build_projects

    rescue ProgramException => e
      puts "*** Aborted!  #{e.message}"
      exit 1
    end
  end

  def build_projects
    Project.map.values.each do |project|
      puts "building project #{project.name}" if @verbose
      @src_path = project.src_path
      @bin_path = project.bin_path

      if @options[:clean]
        FileUtils.process_directory_tree(@bin_path,".class") do |x|
          puts "deleting #{x}..." if @verbose
          FileUtils.rm(x)
        end
      end

      cmd = "javac"
      cmd << " -Xmaxerrs 1"
      cmd << " -cp "
      cmd << class_path
      cmd << " -sourcepath #{project.src_path}"
      cmd << " -d #{project.bin_path}"

      orig_cmd_length = cmd.length
      @cmd = cmd
      test_classes = []

      FileUtils.process_directory_tree(@src_path,".java"){|x| prepare_compile_java_file(x,test_classes)}
      if cmd.length != orig_cmd_length
        puts "Command: #{cmd}" if @verbose
        scall(cmd)
      end

      if !@options[:notests]
        # Run unit tests
        all_tests_succeeded = true
        test_classes.each do |class_name|
          cmd = "java -cp " << class_path << " junit.textui.TestRunner " << class_name
          puts "running unit tests for #{class_name}" if @verbose
          output,success = scall(cmd,false)
          if @verbose || !success
            puts output
          end
          all_tests_succeeded &= success
        end
        raise ProgramException,"Test failures" if !all_tests_succeeded
      end
    end
  end

  def class_path
    if !@class_path
      entries = Set.new
      Project.map.each do |name,project|
        project.verify_dependent_projects_exist
        entries.add(project.bin_path)
        project.libraries.each do |lib|
          entries.add(lib)
        end
      end
      path = ""
      entries.each do |x|
        if path.length > 0
          path << ":"
        end
        path << x
      end
      @class_path = path
    end
    @class_path
  end

  def class_path_for_java_file(src_file)
    rel_path = src_file[@src_path.length+1..-1]
    class_path = FileUtils.change_extension(File.join(@bin_path,rel_path),"class")
    [rel_path,class_path]
  end

  def prepare_compile_java_file(src_file,test_classes)
    # Determine if class file exists and is not older than source
    rel_path,class_file = class_path_for_java_file(src_file)
    if File.file?(class_file) && File.mtime(class_file) >= File.mtime(src_file)
      puts "   (not recompiling valid class file #{class_file})" if @verbose
    else
      @cmd << " " << src_file
    end

    # If class name has "Test" suffix, add to list of test classes
    if rel_path.end_with?("Test.java")
      class_name = FileUtils.remove_extension(rel_path).gsub('/','.')
      test_classes << class_name
    end

  end

  def parse_arguments(argv)
    parser = Trollop::Parser.new do
      banner <<-EOS
      Compiles Java projects
      EOS
      opt :clean, "clean old class files"
      opt :verbose, "display progress"
      opt :notests, "omit unit tests",:short => '-T'
    end
    options = Trollop::with_standard_exception_handling parser do
      parser.parse argv
    end
    options[:leftovers] = parser.leftovers || []
    options
  end

end

if __FILE__ == $0
  App.new.run(ARGV)
end
