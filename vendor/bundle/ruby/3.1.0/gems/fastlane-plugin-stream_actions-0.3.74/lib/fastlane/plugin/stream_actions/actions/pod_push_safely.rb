module Fastlane
  module Actions
    class PodPushSafelyAction < Action
      def self.run(params)
        pod_push_safely(params)
      end

      def self.pod_push_safely(params)
        UI.message("Starting to push podspec: #{params[:podspec]}")
        other_action.pod_push(path: params[:podspec], allow_warnings: true, synchronous: params[:sync])
      rescue StandardError => e
        UI.message(e)
        if e.message.include?('Unable to accept duplicate entry')
          UI.message("pod_push passed for #{params[:podspec]} on previous run. Skipping further attempts.")
        else
          UI.message("pod_push failed for #{params[:podspec]}. Waiting a minute until retry for trunk to get updated...")
          sleep(60) # sleep for a minute, wait until trunk gets updates
          pod_push_safely(params)
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Safely push a Podspec to Trunk or a private repository'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :podspec,
            description: 'The Podspec you want to push'
          ),
          FastlaneCore::ConfigItem.new(
            key: :sync,
            description: 'If validation depends on other recently pushed pods, synchronize',
            is_string: false,
            default_value: false
          )
        ]
      end

      def self.is_supported?(platform)
        [:ios].include?(platform)
      end
    end
  end
end
