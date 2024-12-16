module Fastlane
  module Actions
    module SharedValues
      SIMULATOR_APP_OUTPUT_PATH ||= :SIMULATOR_APP_OUTPUT_PATH
    end

    class BuildAppForIosSimulatorAction < Action
      def self.run(params)
        params[:platform] = verify_destination(params)
        params[:xcargs] = update_xcargs(params)
        params[:archive] = nil if params[:archive]
        params[:configuration] = 'Debug' unless params[:configuration]
        params[:derivedDataPath] = params[:derived_data_path] if params[:derived_data_path]

        clean(params)

        XcodebuildAction.run(params)

        new_path = copy_app(params)

        provide_shared_values(new_path)
        new_path
      end

      def self.clean(params)
        if params[:output_directory]
          FastlaneCore::UI.important('🧹 Clearing output directory.')
          FileUtils.rm_rf(params[:output_directory])
        end

        path = "./#{params[:scheme]}.app"
        if File.directory?(path)
          FastlaneCore::UI.important('🧹 Removing previous build.')
          FileUtils.rm_rf(path)
        end

        path = "build/#{params[:scheme]}.app"
        if File.directory?(path)
          FastlaneCore::UI.important('🧹 Removing previous build.')
          FileUtils.rm_rf(path)
        end
      end

      def self.update_xcargs(params)
        if params[:xcargs] && params[:xcargs].include?('destination')
          FastlaneCore::UI.important(
            '🎭 Overwriting `xcargs` option for safety reasons. Consider excluding `-destination` argument from it.'
          )
          params[:xcargs] = ''
        end

        platform = available_destinations[params[:platform]][:destination]
        destination = "-destination 'generic/platform=#{platform}'"
        products_path = "-IDECustomBuildProductsPath='#{products_path(params)}'"
        if params[:derived_data_path]
          "#{params[:xcargs]} #{products_path} #{destination}"
        else
          derived_data_path = "-derivedDataPath '#{products_path(params)}'"
          "#{params[:xcargs]} #{products_path} #{destination} #{derived_data_path}"
        end
      end

      def self.products_path(params)
        params[:output_directory] || 'build'
      end

      def self.copy_app(params)
        platform = available_destinations[params[:platform]][:folder]
        postfix = "#{params[:configuration]}-#{platform}/#{params[:scheme]}.app"
        products_path = products_path(params)

        products_app_path =
          if params[:project]
            "#{File.dirname(File.expand_path(params[:project]))}/#{products_path}/#{postfix}"
          elsif params[:workspace]
            "#{File.dirname(File.expand_path(params[:workspace]))}/#{products_path}/#{postfix}"
          else
            "#{Dir.pwd}/#{products_path}/#{postfix}"
          end

        derived_data_app_path =
          if params[:derived_data_path]
            "#{params[:derived_data_path]}/Build/Products/#{postfix}"
          else
            "#{Dir.pwd}/#{products_path}/Build/Products/#{postfix}"
          end

        app_path = File.directory?(derived_data_app_path) ? derived_data_app_path : products_app_path

        new_path =
          if params[:output_directory]
            FileUtils.mkdir_p(params[:output_directory])
            FileUtils.cp_r(app_path, params[:output_directory])
            "#{params[:output_directory]}/#{params[:scheme]}.app"
          else
            FileUtils.rm_rf("#{params[:scheme]}.app")
            FileUtils.cp_r(app_path, './')
            "./#{params[:scheme]}.app"
          end

        File.expand_path(new_path)
      end

      def self.verify_destination(params)
        case params[:platform]
        when 'iOS', nil
          :iOS
        when 'tvOS'
          :tvOS
        when 'watchOS'
          :watchOS
        else
          FastlaneCore::UI.user_error!(
            "🔬 Unrecognized platform: '#{params[:platform]}'. Available: 'iOS', 'tvOS' and 'watchOS'"
          )
        end
      end

      def self.available_destinations
        {
          iOS: { destination: 'iOS Simulator', folder: 'iphonesimulator' },
          tvOS: { destination: 'tvOS Simulator', folder: 'appletvsimulator' },
          watchOS: { destination: 'watchOS Simulator', folder: 'applewatchsimulator' }
        }
      end

      def self.provide_shared_values(path)
        Actions.lane_context[SharedValues::SIMULATOR_APP_OUTPUT_PATH] = File.expand_path(path)
        ENV[SharedValues::SIMULATOR_APP_OUTPUT_PATH.to_s] = File.expand_path(path)
      end

      #####################################################
      #                   Documentation                   #
      #####################################################

      def self.description
        "This plugin builds apps exclusively for iOS, tvOS or watchOS Simulators."
      end

      def self.example_code
        [
          build_app_for_simulator(
            scheme: 'sample-app',
            project: 'sample-app/sample-app.xcodeproj',
            configuration: 'Release',
            output_directory: 'build'
          )
        ]
      end

      def self.output
        [
          ['SIMULATOR_APP_OUTPUT_PATH', 'The path to the newly generated app file']
        ]
      end

      def self.available_options
        XcodebuildAction.available_options + [
          ['output_directory', 'The directory in which the app file should be stored in'],
          ['workspace', 'Path to the workspace file'],
          ['project', 'Path to the project file'],
          ['scheme', "The project's scheme. Make sure it's marked as `Shared`"],
          ['platform', "Use a custom simulator destination for building the app (iOS, tvOS or watchOS)"],
          ['configuration', 'The configuration to use when building the app. Defaults to "Debug"'],
          ['derived_data_path', 'The directory where built products and other derived data will go'],
          ['result_bundle_path', 'Path to the result bundle directory to create'],
          ['buildlog_path', 'The directory where to store the build log'],
          ['raw_buildlog', 'Set to true to see xcodebuild raw output'],
          ['xcargs', "Pass additional xcodebuild options. Be sure to quote the setting names and values e.g. OTHER_LDFLAGS='-ObjC -lstdc++'"]
        ]
      end

      def self.category
        :building
      end

      def self.is_supported?(platform)
        [:ios, :tvos, :watchos].include?(platform)
      end
    end
  end
end
