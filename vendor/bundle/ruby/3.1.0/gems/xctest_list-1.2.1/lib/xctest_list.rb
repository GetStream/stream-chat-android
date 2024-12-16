require 'open3'
require 'tempfile'

# A class to parse xctest bundles and return a list of tests that
# are in the bundle's binary.
class XCTestList
  # refactored into its own method to allow mocking in the spec
  def self.system(command)
    `#{command}`
  end

  # validate that the basic bundle parts exist
  def self.validate_bundle(xctest_bundle_path)
    raise "Cannot find xctest bundle at path: '#{xctest_bundle_path}'" unless Dir.exist?(xctest_bundle_path)

    is_xctest_bundle = File.extname(xctest_bundle_path) == '.xctest'
    raise "Invalid xctest bundle given: '#{xctest_bundle_path}'" unless is_xctest_bundle
  end

  # add the expected binary to the bundle path and validate that it exists
  def self.binary_path(xctest_bundle_path)
    validate_bundle(xctest_bundle_path)

    xctest_binary_name = File.basename(xctest_bundle_path, '.*')
    xctest_binary_path = File.join(xctest_bundle_path, xctest_binary_name)
    unless File.exist?(xctest_binary_path)
      xctest_binary_path = File.join(xctest_bundle_path, 'Contents', 'MacOS', xctest_binary_name)
    end

    unless File.exist?(xctest_binary_path)
      raise "Missing xctest binary: '#{xctest_binary_path}'"
    end

    xctest_binary_path
  end

  # find the Objective-C symbols in the bundle's binary
  def self.objc_tests(xctest_bundle_path)
    tests = []
    objc_symbols_command_output_tempfile = Tempfile.new(File.basename(xctest_bundle_path) + "objc")
    system("nm -U '#{binary_path(xctest_bundle_path)}' > '#{objc_symbols_command_output_tempfile.path}'")
    tests = []
    File.foreach(objc_symbols_command_output_tempfile.path) do |line|
      regex = / t -\[(?<testclass>\w+) (?<testmethod>test\w*)\]/
      if (matches = regex.match(line))
        tests << "#{matches[:testclass]}/#{matches[:testmethod]}"
      end
    end
    tests
  end

  def self.swift_symbol_dump_command(xctest_binary_path, swift_test_prefix: "test")
    nm_version_string = `nm -version | head -n 1 | sed -E 's/Apple LLVM version ([0-9]+\.[0-9]+\.[0-9]+).*/\\1/'`.chomp
    if Gem::Version.new(nm_version_string) < Gem::Version.new('9.0.0')
      "nm -gU '#{xctest_binary_path}' | cut -d' ' -f3 | xcrun swift-demangle | grep -E '^\\S+\\.test[^.]+\\(\\)$' | sed 's/ () -> ()/()/'"
    else
      "nm -gU '#{xctest_binary_path}' | cut -d' ' -f3 | xargs -s 131072 xcrun swift-demangle | cut -d' ' -f3 | grep -e '[\\.|_]'#{swift_test_prefix}"
    end
  end

  # find the Swift symbols in the bundle's binary
  def self.swift_tests(xctest_bundle_path, swift_test_prefix: "test")
    swift_symbols_command_output_tempfile = Tempfile.new(File.basename(xctest_bundle_path) + "swift")
    system("#{swift_symbol_dump_command(binary_path(xctest_bundle_path), swift_test_prefix: swift_test_prefix)}  > '#{swift_symbols_command_output_tempfile.path}'")
    tests = []
    File.foreach(swift_symbols_command_output_tempfile.path) do |line|
      regex = /\w+\.(?<testclass>[^\.]+)\.(?<testmethod>#{swift_test_prefix}[^\(]*)\(/
      if /.*-\[.*\]/ !~ line && (matches = regex.match(line))
        tests << "#{matches[:testclass]}/#{matches[:testmethod]}"
      end
    end
    tests
  end

  # find the Objective-C and Swift tests in the binary's bundle
  def self.tests(xctest_bundle_path, swift_test_prefix: 'test')
    objc_tests(xctest_bundle_path) | swift_tests(xctest_bundle_path, swift_test_prefix: swift_test_prefix)
  end
end
