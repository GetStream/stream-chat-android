require 'plist'
require 'xctest_list'

module Fastlane
  module Actions
    class RetrieveXctestNamesAction < Action
      def self.run(params)
        UI.verbose("Getting tests from xctestrun file at '#{params[:xctestrun]}'")
        xctestrun_tests(params[:xctestrun])
      end

      def self.xctestrun_tests(xctestrun_path)
        xctestrun = Plist.parse_xml(xctestrun_path)
        xctestrun_rootpath = File.dirname(xctestrun_path)
        xctestrun_version = xctestrun.fetch(xctestrun_metadata) { {} }.fetch('FormatVersion') { 1 }

        test_targets = []
        if xctestrun_version == 1
          xctestrun.each do |testable_name, test_target_config|
            next if testable_name == xctestrun_metadata

            test_target_config['TestableName'] = testable_name
            test_targets << test_target_config
          end
        else
          test_configurations = xctestrun['TestConfigurations']
          test_configurations.each do |configuration|
            configuration['TestTargets'].each do |test_target|
              test_target['TestableName'] = test_target['BlueprintName']
              test_targets << test_target
            end
          end
        end

        tests = {}
        test_targets.each do |xctestrun_config|
          testable_name = xctestrun_config['TestableName']
          xctest_path = xctest_bundle_path(xctestrun_rootpath, xctestrun_config)
          test_identifiers = []
          if xctestrun_config.key?('OnlyTestIdentifiers')
            test_identifiers = xctestrun_config['OnlyTestIdentifiers']
            UI.verbose("Identifiers after adding onlytest tests: #{test_identifiers.join("\n\t")}")
          else
            test_identifiers = XCTestList.tests(xctest_path)
            UI.verbose("Found the following tests: #{test_identifiers.join("\n\t")}")
          end
          if xctestrun_config.key?('SkipTestIdentifiers')
            test_identifiers = subtract_skipped_tests_from_test_identifiers(
              test_identifiers,
              xctestrun_config['SkipTestIdentifiers']
            )
            UI.verbose("Identifiers after removing skipped tests: #{test_identifiers.join("\n\t")}")
          end
          if test_identifiers.empty?
            UI.error("No tests found in '#{xctest_path}'!")
            UI.important("Check `ENABLE_TESTABILITY` build setting in `#{testable_name}` test target.")
          end
          tests[testable_name] = test_identifiers.map { |test_identifier| "#{testable_name}/#{test_identifier}" }
        end
        tests
      end

      def self.subtract_skipped_tests_from_test_identifiers(test_identifiers, skipped_test_identifiers)
        skipped_tests_identifiers = []
        skipped_testsuites = []
        skipped_test_identifiers.each do |skipped_test|
          if skipped_test.split('/').size > 1
            skipped_tests_identifiers << skipped_test
          else
            skipped_testsuites << skipped_test
          end
        end
        skipped_testsuites.each do |skipped_testsuite|
          derived_skipped_tests = test_identifiers.select do |test_identifier|
            test_identifier.start_with?(skipped_testsuite)
          end
          skipped_tests_identifiers.concat(derived_skipped_tests)
        end

        UI.verbose("Removing skipped tests: #{skipped_tests_identifiers.join("\n\t")}")
        test_identifiers.reject { |test_identifier| skipped_tests_identifiers.include?(test_identifier) }
      end

      def self.xctest_bundle_path(xctestrun_rootpath, xctestrun_config)
        xctest_host_path = xctestrun_config['TestHostPath'].sub('__TESTROOT__', xctestrun_rootpath)
        xctestrun_config['TestBundlePath'].sub('__TESTHOST__', xctest_host_path).sub('__TESTROOT__', xctestrun_rootpath)
      end

      def self.xctestrun_metadata
        '__xctestrun_metadata__'
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Retrieves test names from xctestrun file'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :xctestrun,
            description: 'The xctestrun file path',
            verify_block: proc do |path|
              UI.user_error!("Cannot find the xctestrun file '#{path}'") unless File.exist?(path)
            end
          )
        ]
      end

      def self.is_supported?(platform)
        [:ios].include?(platform)
      end
    end
  end
end
