module Fastlane
  module Actions
    class UpdateCopyrightAction < Action
      def self.run(params)
        new_year = Time.now.year.to_s

        ext = '*.{swift,h,strings,pbxproj}'
        source_files = Dir.glob(File.join(params[:path], '**', ext)).reject do |f|
          params[:ignore].any? { |dir| f.include?(dir) }
        end

        source_files.each do |file_path|
          UI.message("👀 Searching for copyright header in #{File.absolute_path(file_path)}")
          old_content = File.read(file_path)
          match = old_content.match(/Copyright © (\d{4}) Stream.io/)
          next if !match || match[1] == new_year

          old_year = match[1]
          new_content = old_content.gsub("Copyright © #{old_year}", "Copyright © #{new_year}")

          File.write(file_path, new_content)
          UI.success("✅ Updated copyright header in #{File.absolute_path(file_path)}")
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Updates copyright headers in source files'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :path,
            description: 'A path to search for files to update',
            default_value: '.'
          ),
          FastlaneCore::ConfigItem.new(
            key: :ignore,
            description: 'The folders to ignore',
            is_string: false,
            default_value: []
          )
        ]
      end

      def self.is_supported?(platform)
        [:ios].include?(platform)
      end
    end
  end
end
