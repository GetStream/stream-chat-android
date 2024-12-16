module Fastlane
  module Actions
    class UpdateTestplanAction < Action
      def self.run(params)
        data_hash = JSON.parse(File.read(params[:path]))

        data_hash['defaultOptions']['environmentVariableEntries'] ||= []

        if params[:env_vars].kind_of?(Hash)
          data_hash['defaultOptions']['environmentVariableEntries'] << params[:env_vars]
        else
          params[:env_vars].each { |env| data_hash['defaultOptions']['environmentVariableEntries'] << env }
        end

        File.write(params[:path], JSON.pretty_generate(data_hash))

        UI.message("👀 Testplan environment variables:\n#{data_hash['defaultOptions']['environmentVariableEntries']}")
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Adds environment variables to a test plan'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :path,
            description: 'The test plan file path',
            verify_block: proc do |path|
              UI.user_error!("Cannot find the testplan file '#{path}'") unless File.exist?(path)
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :env_vars,
            description: 'The environment variables to add to test plan',
            is_string: false,
            verify_block: proc do |env_vars|
              UI.user_error!("The environment variables array should not be empty") if env_vars.nil? || env_vars.empty?
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
