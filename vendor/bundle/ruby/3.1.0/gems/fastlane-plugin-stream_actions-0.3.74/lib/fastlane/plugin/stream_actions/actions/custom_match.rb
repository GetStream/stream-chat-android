module Fastlane
  module Actions
    class CustomMatchAction < Action
      def self.run(params)
        if params[:register_device]
          device_name = other_action.prompt(text: 'Enter the device name: ')
          device_udid = other_action.prompt(text: 'Enter the device UDID: ')
          other_action.register_device(name: device_name, udid: device_udid)
          params[:readonly] = false
        end

        %w[development adhoc appstore].each do |type|
          other_action.match(
            api_key: params[:api_key],
            type: type,
            app_identifier: params[:app_identifier],
            readonly: params[:readonly],
            force_for_new_devices: !other_action.is_ci
          )
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Installs or recreates all Certs and Profiles necessary for development and ad-hoc and registers a new device if required'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :api_key,
            description: 'AppStore Connect API Key',
            is_string: false,
            verify_block: proc do |api_key|
              UI.user_error!('AppStore Connect API Key has to be specified') if api_key.nil? || api_key.empty? || !api_key.kind_of?(Hash)
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :app_identifier,
            description: 'The bundle identifier(s) of your app (array of strings)',
            is_string: false,
            verify_block: proc do |id|
              UI.user_error!("The bundle identifier(s) have to be specified") unless id.kind_of?(Array) && id.size.positive?
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :readonly,
            is_string: false,
            default_value: true,
            description: 'If `readonly: true` (by default), installs all Certs and Profiles necessary for development and ad-hoc.\nIf `readonly: false`, recreates all Profiles necessary for development and ad-hoc, updates them locally and remotely'
          ),
          FastlaneCore::ConfigItem.new(
            key: :register_device,
            is_string: false,
            default_value: false,
            description: 'When `true` you will be asked to specify a name and UDID of new device to register'
          )
        ]
      end

      def self.is_supported?(platform)
        [:ios].include?(platform)
      end
    end
  end
end
