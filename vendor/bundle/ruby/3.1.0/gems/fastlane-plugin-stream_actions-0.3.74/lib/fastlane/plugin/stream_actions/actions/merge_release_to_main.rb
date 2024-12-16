module Fastlane
  module Actions
    class MergeReleaseToMainAction < Action
      def self.run(params)
        other_action.ensure_git_status_clean

        release_branch =
          if other_action.is_ci
            # This API operation needs the "admin:org" scope.
            ios_team = `gh api orgs/GetStream/teams/#{params[:github_team_name]}/members -q '.[].login'`.split("\n")
            UI.user_error!("#{params[:author]} is not a member of the iOS Team") unless ios_team.include?(params[:author])

            other_action.current_branch
          else
            release_branches = sh(command: 'git branch -a').delete(' ').split("\n").grep(%r(origin/.*release/))
            UI.user_error!("Expected 1 release branch, found #{release_branches.size}") if release_branches.size != 1

            release_branches.first
          end

        UI.user_error!("`#{release_branch}`` branch does not match the release branch pattern: `release/*`") unless release_branch.start_with?('release/')

        sh('git config pull.ff only')
        sh('git fetch --all --tags --prune')
        sh("git checkout #{release_branch}")
        sh("git pull origin #{release_branch} --ff-only")
        sh('git checkout main')
        sh('git pull origin main --ff-only')
        sh("git merge #{release_branch} --ff-only")
        sh('git push origin main')

        comment = "Publication of the release has been launched 👍"
        UI.important(comment)
        other_action.pr_comment(text: comment)
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Merge release branch to main'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            env_name: 'USER_LOGIN',
            key: :author,
            description: 'Github user name',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :github_team_name,
            description: 'Github team name',
            default_value: 'ios-developers'
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
