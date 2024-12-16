module Fastlane
  module Actions
    class DangerAction < Action
      def self.run(params)
        Actions.verify_gem!('danger')
        cmd = []

        cmd << 'bundle exec' if params[:use_bundle_exec] && shell_out_should_use_bundle_exec?
        cmd << 'danger'
        cmd << '--verbose' if params[:verbose]

        danger_id = params[:danger_id]
        dangerfile = params[:dangerfile]
        base = params[:base]
        head = params[:head]
        pr = params[:pr]
        cmd << "--danger_id=#{danger_id}" if danger_id
        cmd << "--dangerfile=#{dangerfile}" if dangerfile
        cmd << "--fail-on-errors=true" if params[:fail_on_errors]
        cmd << "--fail-if-no-pr=true" if params[:fail_if_no_pr]
        cmd << "--new-comment" if params[:new_comment]
        cmd << "--remove-previous-comments" if params[:remove_previous_comments]
        cmd << "--base=#{base}" if base
        cmd << "--head=#{head}" if head
        cmd << "pr #{pr}" if pr

        ENV['DANGER_GITHUB_API_TOKEN'] = params[:github_api_token] if params[:github_api_token]
        ENV['DANGER_GITHUB_HOST'] = params[:github_enterprise_host] if params[:github_enterprise_host]
        ENV['DANGER_GITHUB_API_BASE_URL'] = params[:github_enterprise_api_base_url] if params[:github_enterprise_api_base_url]

        Actions.sh(cmd.join(' '))
      end

      def self.description
        "Runs `danger` for the project"
      end

      def self.details
        [
          "Formalize your Pull Request etiquette.",
          "More information: [https://github.com/danger/danger](https://github.com/danger/danger)."
        ].join("\n")
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(key: :use_bundle_exec,
                                       env_name: "FL_DANGER_USE_BUNDLE_EXEC",
                                       description: "Use bundle exec when there is a Gemfile presented",
                                       type: Boolean,
                                       default_value: true),
          FastlaneCore::ConfigItem.new(key: :verbose,
                                       env_name: "FL_DANGER_VERBOSE",
                                       description: "Show more debugging information",
                                       type: Boolean,
                                       default_value: false),
          FastlaneCore::ConfigItem.new(key: :danger_id,
                                       env_name: "FL_DANGER_ID",
                                       description: "The identifier of this Danger instance",
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :dangerfile,
                                       env_name: "FL_DANGER_DANGERFILE",
                                       description: "The location of your Dangerfile",
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :github_api_token,
                                       env_name: "FL_DANGER_GITHUB_API_TOKEN",
                                       description: "GitHub API token for danger",
                                       sensitive: true,
                                       code_gen_sensitive: true,
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :github_enterprise_host,
                                       env_name: "FL_DANGER_GITHUB_ENTERPRISE_HOST",
                                       description: "GitHub host URL for GitHub Enterprise",
                                       sensitive: true,
                                       code_gen_sensitive: true,
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :github_enterprise_api_base_url,
                                       env_name: "FL_DANGER_GITHUB_ENTERPRISE_API_BASE_URL",
                                       description: "GitHub API base URL for GitHub Enterprise",
                                       sensitive: true,
                                       code_gen_sensitive: true,
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :fail_on_errors,
                                       env_name: "FL_DANGER_FAIL_ON_ERRORS",
                                       description: "Should always fail the build process, defaults to false",
                                       type: Boolean,
                                       optional: true,
                                       default_value: false),
          FastlaneCore::ConfigItem.new(key: :new_comment,
                                       env_name: "FL_DANGER_NEW_COMMENT",
                                       description: "Makes Danger post a new comment instead of editing its previous one",
                                       type: Boolean,
                                       optional: true,
                                       default_value: false),
          FastlaneCore::ConfigItem.new(key: :remove_previous_comments,
                                       env_name: "FL_DANGER_REMOVE_PREVIOUS_COMMENT",
                                       description: "Makes Danger remove all previous comment and create a new one in the end of the list",
                                       type: Boolean,
                                       optional: true,
                                       default_value: false),
          FastlaneCore::ConfigItem.new(key: :base,
                                       env_name: "FL_DANGER_BASE",
                                       description: "A branch/tag/commit to use as the base of the diff. [master|dev|stable]",
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :head,
                                       env_name: "FL_DANGER_HEAD",
                                       description: "A branch/tag/commit to use as the head. [master|dev|stable]",
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :pr,
                                       env_name: "FL_DANGER_PR",
                                       description: "Run danger on a specific pull request. e.g. \"https://github.com/danger/danger/pull/518\"",
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :fail_if_no_pr,
                                       env_name: "FL_DANGER_FAIL_IF_NO_PR",
                                       description: "Fail Danger execution if no PR is found",
                                       type: Boolean,
                                       default_value: false)
        ]
      end

      def self.is_supported?(platform)
        true
      end

      def self.example_code
        [
          'danger',
          'danger(
            danger_id: "unit-tests",
            dangerfile: "tests/MyOtherDangerFile",
            github_api_token: ENV["GITHUB_API_TOKEN"],
            verbose: true
          )'
        ]
      end

      def self.category
        :misc
      end

      def self.authors
        ["KrauseFx"]
      end
    end
  end
end
