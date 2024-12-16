module Fastlane
  module Actions
    class SetupGitConfigAction < Action
      def self.run(params)
        params[:username] ||= 'Stream Bot'
        sh("git config --global user.name '#{params[:username]}'")
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Update git config details'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :username,
            description: 'Username',
            optional: true
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
