module Fastlane
  module Actions
    class CurrentBranchAction < Action
      def self.run(params)
        branch = if params[:pr_num].to_s.empty?
                   other_action.git_branch
                 else
                   sh("gh pr view #{params[:pr_num]} --json headRefName -q .headRefName").strip
                 end

        UI.important("Current branch: #{branch} 🕊️")
        branch
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Get current branch name'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            env_name: 'GITHUB_PR_NUM',
            key: :pr_num,
            description: 'GitHub PR number',
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
