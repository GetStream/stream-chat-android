module Fastlane
  module Actions
    class AllureRunTestplanAction < Action
      def self.run(params)
        response = other_action.allure_api(
          url: params[:url],
          token: params[:token],
          path: "testplan/?projectId=#{params[:project_id]}",
          http_method: 'GET'
        )

        testplan_id = response['content'].find { |plan| plan['name'] == params[:testplan] }['id']

        other_action.allure_api(
          url: params[:url],
          token: params[:token],
          path: "testplan/#{testplan_id}/sync",
          http_method: 'POST'
        )
        UI.success("Testplan with id #{testplan_id} synced successfully 🎉")

        body = {
          launchName: "#{params[:testplan]} #{params[:release_version]}",
          links: [{ name: 'Jira', url: params[:jira] }]
        }.to_json

        other_action.allure_api(
          url: params[:url],
          token: params[:token],
          path: "testplan/#{testplan_id}/run",
          http_method: 'POST',
          request_body: body
        )
        UI.success("Testplan with id #{testplan_id} launched successfully 🎉")
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Sync and run testplan on Allure Testops'
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
            key: :release_version,
            description: 'Release version',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :testplan,
            description: 'Testplan name in Allure Testops'
          ),
          FastlaneCore::ConfigItem.new(
            key: :project_id,
            description: 'Project identifier in Allure Testops'
          ),
          FastlaneCore::ConfigItem.new(
            key: :jira,
            description: 'Jira link',
            default_value: 'Default'
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
