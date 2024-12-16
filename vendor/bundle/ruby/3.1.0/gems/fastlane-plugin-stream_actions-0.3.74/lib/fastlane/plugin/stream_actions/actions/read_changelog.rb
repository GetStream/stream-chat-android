module Fastlane
  module Actions
    class ReadChangelogAction < Action
      def self.run(params)
        UI.message("Getting changelog for #{params[:version]}")
        reading_changelog = false
        changes = ''
        changelog_lines = File.readlines(params[:changelog_path])
        changelog_lines.each do |line|
          start_token = '# ['
          if reading_changelog
            break if line.start_with?(start_token)

            changes << line
          end

          reading_changelog = true if line.start_with?("#{start_token}#{params[:version]}")
        end

        UI.user_error!("No changelog found for #{params[:version]}") unless changes.length > 0
        UI.success("Changelog for #{params[:version]}: \n#{changes}")
        changes
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Gets updates from the CHANGELOG.md'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :version,
            description: 'Release version',
            verify_block: proc do |v|
              UI.user_error!("You need to pass the version of the release you want to obtain the changelog from") if v.nil? || v.empty?
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :changelog_path,
            env_name: 'FL_CHANGELOG_PATH',
            description: 'The path to your project CHANGELOG.md',
            is_string: true,
            default_value: './CHANGELOG.md',
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
