module Fastlane
  module Actions
    class ReleaseIosSdkAction < Action
      def self.run(params)
        podspecs = []
        (params[:podspec_names] || params[:sdk_names]).each do |sdk|
          podspecs << (sdk.include?('.podspec') ? sdk : "#{sdk}.podspec")
        end

        ensure_everything_is_set_up(params)

        version_number = ''
        params[:sdk_names].each do |target|
          version_number = other_action.increment_version_number_in_plist(
            target: target,
            version_number: params[:version],
            bump_type: params[:bump_type]
          )
        end

        ensure_release_tag_is_new(version_number)

        changes = other_action.touch_changelog(
          release_version: version_number,
          github_repo: params[:github_repo],
          changelog_path: params[:changelog_path]
        )

        podspecs.each do |podspec|
          UI.user_error!("Podspec #{podspec} does not exist!") unless File.exist?(podspec)
          other_action.version_bump_podspec(path: podspec, version_number: version_number)
        end

        params[:extra_changes].call(version_number) if params[:extra_changes]

        sh("git checkout -b release/#{version_number}") if params[:create_pull_request]

        commit_changes(version_number)

        if params[:create_pull_request]
          other_action.create_pull_request(
            api_token: params[:github_token],
            repo: params[:github_repo],
            title: "#{version_number} Release",
            head: "release/#{version_number}",
            base: 'main',
            body: changes.to_s
          )
        end

        UI.success("Successfully started release #{version_number}! 🚢")
        version_number
      end

      def self.ensure_everything_is_set_up(params)
        other_action.ensure_git_status_clean unless params[:skip_git_status_check]

        if params[:version].nil? && !["patch", "minor", "major"].include?(params[:bump_type])
          UI.user_error!("Please use type parameter with one of the options: type:patch, type:minor, type:major")
        end
      end

      def self.ensure_release_tag_is_new(version_number)
        if other_action.git_tag_exists(tag: version_number)
          UI.user_error!("Tag for version #{version_number} already exists!")
        else
          UI.success("Ignore the red warning above. Tag for version #{version_number} is alright!")
        end
      end

      def self.commit_changes(version_number)
        sh("git add -A")
        sh("git commit -m 'Bump #{version_number}'")
        other_action.push_to_git_remote(tags: false)
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Releases iOS SDKs'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :version,
            description: 'Release version (not required if release type is set)',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :bump_type,
            description: 'Release type (not required if release version is set)',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :sdk_names,
            description: 'SDK names to release',
            is_string: false,
            verify_block: proc do |sdks|
              UI.user_error!("SDK names array has to be specified") unless sdks.kind_of?(Array) && sdks.size.positive?
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :podspec_names,
            description: 'Podspec names to release',
            is_string: false,
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            env_name: 'GITHUB_REPOSITORY',
            key: :github_repo,
            description: 'Github repository name'
          ),
          FastlaneCore::ConfigItem.new(
            env_name: 'GITHUB_TOKEN',
            key: :github_token,
            description: 'GITHUB_TOKEN environment variable'
          ),
          FastlaneCore::ConfigItem.new(
            key: :skip_git_status_check,
            description: 'Skip git status check',
            is_string: false,
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :changelog_path,
            env_name: 'FL_CHANGELOG_PATH',
            description: 'The path to your project CHANGELOG.md',
            is_string: true,
            default_value: './CHANGELOG.md'
          ),
          FastlaneCore::ConfigItem.new(
            key: :extra_changes,
            description: 'Lambda with extra changes to be commited to the release',
            is_string: false,
            default_value: false
          ),
          FastlaneCore::ConfigItem.new(
            key: :create_pull_request,
            description: 'Create pull request from release branch to main',
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
