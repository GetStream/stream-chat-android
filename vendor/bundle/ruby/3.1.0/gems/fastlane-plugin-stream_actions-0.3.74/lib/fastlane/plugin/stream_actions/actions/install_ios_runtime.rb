module Fastlane
  module Actions
    class InstallIosRuntimeAction < Action
      def self.run(params)
        runtimes = `xcrun simctl runtime list -j`
        UI.message("👉 Runtime list:\n#{runtimes}")
        simulators = JSON.parse(runtimes).select do |_, sim|
          sim['platformIdentifier'].end_with?('iphonesimulator') && sim['version'] == params[:version] && sim['state'] == 'Ready'
        end

        if simulators.empty?
          if params[:tool] == 'ipsw'
            sh("echo 'iOS #{params[:version]} Simulator' | ipsw download xcode --sim") if Dir['*.dmg'].first.nil?
            sh("#{params[:custom_script]} #{Dir['*.dmg'].first}") if params[:custom_script]
          else
            sh("sudo xcodes runtimes install 'iOS #{params[:version]}'")
          end
          UI.success("iOS #{params[:version]} Runtime successfuly installed")
        else
          UI.important("iOS #{params[:version]} Runtime already exists")
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Install iOS Runtime'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :version,
            description: 'iOS Version'
          ),
          FastlaneCore::ConfigItem.new(
            key: :tool,
            description: 'Which tool to use to install the runtime: ipsw or xcodes',
            default_value: 'ipsw',
            verify_block: proc do |tool|
              UI.user_error!('Available options are `ipsw` and `xcodes`') unless ['xcodes', 'ipsw'].include?(tool)
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :custom_script,
            description: 'Path to custom script to install the runtime (might be required for ipsw)',
            optional: true
          )
        ]
      end

      def self.is_supported?(platform)
        [:ios].include?(platform)
      end
    end
  end
end
