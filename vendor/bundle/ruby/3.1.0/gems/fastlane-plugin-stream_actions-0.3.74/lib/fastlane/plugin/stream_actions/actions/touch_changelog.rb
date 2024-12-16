module Fastlane
  module Actions
    class TouchChangelogAction < Action
      def self.run(params)
        UI.message("Starting to update '#{params[:changelog_path]}'")

        file_data = File.readlines(params[:changelog_path])
        upcoming_line = -1
        changes_since_last_release = ''

        File.open(params[:changelog_path]).each.with_index do |line, index|
          if upcoming_line != -1
            break if line.start_with?('# [')

            changes_since_last_release += line
          elsif line == "# Upcoming\n"
            upcoming_line = index
          end
        end

        file_data[upcoming_line] = "# [#{params[:release_version]}](https://github.com/#{params[:github_repo]}/releases/tag/#{params[:release_version]})"

        today = Time.now.strftime('%B %d, %Y')

        file_data.insert(upcoming_line + 1, "_#{today}_")
        file_data.insert(upcoming_line, '# Upcoming')
        file_data.insert(upcoming_line + 1, '')
        file_data.insert(upcoming_line + 2, '### 🔄 Changed')
        file_data.insert(upcoming_line + 3, '')

        changelog = File.open(params[:changelog_path], 'w')
        changelog.puts(file_data)
        changelog.close
        UI.success("Successfully updated #{params[:changelog_path]}")

        changes_since_last_release
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Updates CHANGELOG.md file with release'
      end

      def self.details
        'Use this action to rename your unrelease section to your release version and add a new unreleased section to your project CHANGELOG.md'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :github_repo,
            env_name: 'GITHUB_REPOSITORY',
            description: 'The owner and repository name. For example, octocat/Hello-World'
          ),
          FastlaneCore::ConfigItem.new(
            key: :changelog_path,
            description: 'The path to your project CHANGELOG.md',
            default_value: './CHANGELOG.md'
          ),
          FastlaneCore::ConfigItem.new(
            key: :release_version,
            description: 'The release version, according to semantic versioning'
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
