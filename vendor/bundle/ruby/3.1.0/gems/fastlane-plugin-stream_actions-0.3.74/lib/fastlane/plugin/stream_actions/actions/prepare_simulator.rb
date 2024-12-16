module Fastlane
  module Actions
    class PrepareSimulatorAction < Action
      def self.run(params)
        simulators = FastlaneCore::Simulator.all
        version_regex = /\((\d+\.)?(\d+\.)?(\*|\d+)\)/
        ios_version_with_brackets = params[:device][version_regex]
        device_name = params[:device].sub(version_regex, '').strip
        udid = nil

        if ios_version_with_brackets.nil?
          sim = simulators.filter { |d| d.name == params[:device] }.max_by(&:os_version)
          ios_version_with_brackets = "(#{sim.os_version})" if sim
        else
          ios_version = ios_version_with_brackets.delete('()')
          sim = simulators.detect { |d| "#{d.name} (#{d.os_version})" == params[:device] }
          udid = `xcrun simctl create '#{device_name}' '#{device_name}' 'iOS#{ios_version}'`.to_s.strip if sim.nil?
        end

        udid = sim.udid.to_s.strip unless sim.nil?

        if sim.nil? && (udid.nil? || udid.empty?)
          simulators.map! { |d| "#{d.name} (#{d.os_version})" }.join("\n")
          UI.user_error!("Simulator #{params[:device]} not found \nAvailable simulators: \n#{simulators}")
        end

        sim.reset if sim && params[:reset]
        sh("xcrun simctl bootstatus #{udid} -b")
        UI.success("Simulator #{device_name} #{ios_version_with_brackets} is ready")
        udid
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Prepares simulator for tests'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :device,
            description: 'Simulator name or name with version',
            is_string: false
          ),
          FastlaneCore::ConfigItem.new(
            key: :reset,
            description: 'Reset simulator contents',
            optional: true,
            is_string: false
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
