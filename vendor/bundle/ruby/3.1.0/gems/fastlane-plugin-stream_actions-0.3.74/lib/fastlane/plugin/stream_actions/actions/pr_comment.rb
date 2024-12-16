module Fastlane
  module Actions
    class PrCommentAction < Action
      def self.run(params)
        if params[:pr_num].to_s.empty?
          UI.important('Skipping the PR comment.')
        else
          additional_args = []
          if params[:edit_last_comment_with_text]
            UI.message('Checking last comment for required pattern.')
            last_comment = sh("gh pr view #{params[:pr_num]} --json comments --jq '.comments | map(select(.author.login == \"Stream-SDK-Bot\")) | last'")
            last_comment_match = params[:edit_last_comment_with_text] && last_comment.include?(params[:edit_last_comment_with_text])

            if last_comment_match
              additional_args << '--edit-last'
            else
              UI.important('Last comment does not match the pattern.')
            end
          end
          sh("gh pr comment #{params[:pr_num]} -b '#{params[:text]}' #{additional_args.join(' ')}")
          UI.success('PR comment been added.')
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Comment in the PR'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            env_name: 'GITHUB_PR_NUM',
            key: :pr_num,
            description: 'GitHub PR number',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :text,
            description: 'Comment text',
            is_string: true,
            verify_block: proc do |text|
              UI.user_error!("Text should not be empty") if text.to_s.empty?
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :edit_last_comment_with_text,
            description: 'If last comment contains this text it will be edited',
            is_string: true,
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
