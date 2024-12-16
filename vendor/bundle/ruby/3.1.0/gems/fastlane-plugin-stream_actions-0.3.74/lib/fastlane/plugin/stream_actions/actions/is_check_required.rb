module Fastlane
  module Actions
    class IsCheckRequiredAction < Action
      def self.run(params)
        return true if params[:force_check] || params[:github_pr_num].nil? || params[:github_pr_num].strip.empty?

        UI.message("Checking if check is required for PR ##{params[:github_pr_num]}")

        changed_files = Actions.sh("gh pr view #{params[:github_pr_num]} --json files -q '.files[].path'").split("\n")

        too_many_files = changed_files.size > 99 # TODO: https://github.com/cli/cli/issues/5368
        if too_many_files
          UI.important("Check it required because there were too many files changed.")
          return true
        end

        changed_files.select! do |path|
          params[:sources].any? { |required| path.start_with?(required) }
        end

        is_check_required = changed_files.size.positive?
        UI.important("Check is required: #{is_check_required}")
        is_check_required
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Analyzes the impact of changes on PR'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :sources,
            description: 'Array of paths to scan',
            is_string: false,
            verify_block: proc do |array|
              UI.user_error!("Sources have to be specified") unless array.kind_of?(Array) && array.size.positive?
            end
          ),
          FastlaneCore::ConfigItem.new(
            env_name: 'GITHUB_PR_NUM',
            key: :github_pr_num,
            description: 'GitHub PR number',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :force_check,
            description: 'GitHub PR number',
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
