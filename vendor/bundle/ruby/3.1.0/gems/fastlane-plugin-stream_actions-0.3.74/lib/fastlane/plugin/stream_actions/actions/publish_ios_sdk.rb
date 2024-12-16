module Fastlane
  module Actions
    class PublishIosSdkAction < Action
      def self.run(params)
        version_number = params[:version]

        ensure_everything_is_set_up(params)
        ensure_release_tag_is_new(version_number)

        changes = params[:changelog] || other_action.read_changelog(version: version_number, changelog_path: params[:changelog_path])

        release_details = other_action.set_github_release(
          repository_name: params[:github_repo],
          api_token: params[:github_token],
          name: version_number,
          tag_name: version_number,
          description: changes,
          commitish: other_action.current_branch,
          upload_assets: params[:upload_assets]
        )

        unless params[:skip_pods]
          podspecs = []
          (params[:podspec_names] || params[:sdk_names]).each do |sdk|
            podspecs << (sdk.include?('.podspec') ? sdk : "#{sdk}.podspec")
          end
          podspecs.each { |podspec| other_action.pod_push_safely(podspec: podspec) }
        end

        UI.success("Github release v#{version_number} was created, please visit #{release_details['html_url']} to see it! 🚢")
      end

      def self.ensure_everything_is_set_up(params)
        other_action.ensure_git_branch(branch: 'main') unless params[:skip_branch_check]
        other_action.ensure_git_status_clean unless params[:skip_git_status_check]
      end

      def self.ensure_release_tag_is_new(version_number)
        if other_action.git_tag_exists(tag: version_number)
          UI.user_error!("Tag for version #{version_number} already exists!")
        else
          UI.success("Ignore the red warning above. Tag for version #{version_number} is alright!")
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Publish iOS SDKs'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :version,
            description: 'Release version (not required if release type is set)'
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
            key: :skip_pods,
            description: 'Skip release to CocoaPods?',
            is_string: false,
            optional: true,
            default_value: false
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
            key: :skip_branch_check,
            description: 'Skip branch check',
            is_string: false,
            optional: true
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
            key: :changelog,
            description: 'Static changelog',
            is_string: true,
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :upload_assets,
            description: 'Path to assets to be uploaded with the release',
            is_string: false,
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
