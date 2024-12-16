module Fastlane
  module Actions
    class AllureCreateLaunchAction < Action
      def self.run(params)
        tags = []

        launch_name =
          if params[:cron]
            'Cron checks'
          elsif params[:github_run_details].nil?
            'Local checks'
          elsif params[:github_event_name] == 'pull_request'
            pull_request = JSON.parse(params[:github_event])['pull_request']
            tags = pull_request['labels'].map { |label| { name: label['name'] } }
            pull_request['title']
          elsif params[:github_run_details]['head_commit']
            params[:github_run_details]['head_commit']['message']
          else
            params[:github_run_details]['name']
          end

        body = {
          projectId: params[:project_id],
          name: launch_name,
          tags: tags,
          autoclose: false,
          closed: false
        }.to_json

        launch_id = other_action.allure_api(
          url: params[:url],
          token: params[:token],
          path: 'launch',
          http_method: 'POST',
          request_body: body
        )['id']

        UI.success("Launch with id #{launch_id} created successfully 🎉")
        launch_id
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Creates launch on Allure Testops'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            env_name: 'ALLURE_TOKEN',
            key: :token,
            description: 'Allure API Token'
          ),
          FastlaneCore::ConfigItem.new(
            key: :url,
            description: 'Testops URL'
          ),
          FastlaneCore::ConfigItem.new(
            key: :github_run_details,
            description: 'Github run details json',
            is_string: false,
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            env_name: 'GITHUB_EVENT_NAME',
            key: :github_event_name,
            description: 'Github event name',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            env_name: 'GITHUB_EVENT',
            key: :github_event,
            description: 'Github Actions: toJson(github.event)',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :project_id,
            description: 'Project identifier in Allure Testops'
          ),
          FastlaneCore::ConfigItem.new(
            key: :cron,
            description: 'Is this a cron job?',
            is_string: false,
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
