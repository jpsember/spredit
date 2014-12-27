#!/usr/bin/env ruby

# Run this script to extablish symbolic links to external libraries

require 'fileutils'
require 'js_base'

SRC = File.absolute_path(File.dirname(__FILE__))
BIN = "/usr/local/bin"

def info(msg)
  puts msg if VERBOSE
end

def link(path)
  path = File.expand_path(path)
  dest = File.basename(path)
  bname = File.basename(dest)

  src = path
  info " source: #{src}; exists=#{File.exist?(src)}"
  fail if !File.exist?(src)

  puts "linking #{dest} --> #{src}"
  FileUtils.ln_sf(src,dest)
end

link "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/ui.jar"
link "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Classes/classes.jar"
link "~/jogamp/jogl/build/jar/jogl-all.jar"
link "~/jogamp/gluegen/build/gluegen-rt.jar"
