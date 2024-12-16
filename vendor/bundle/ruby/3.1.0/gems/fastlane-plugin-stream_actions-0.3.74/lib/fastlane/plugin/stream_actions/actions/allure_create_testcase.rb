module Fastlane
  module Actions
    class AllureCreateTestcaseAction < Action
      def self.run(params)
        body = { projectId: params[:project_id], name: 'Automatically created testcase', deleted: true }.to_json

        testcase_id = other_action.allure_api(
          url: params[:url],
          token: params[:token],
          path: 'testcase',
          http_method: 'POST',
          request_body: body
        )['id']

        UI.success("Testcase with id #{testcase_id} created successfully 🎉")
        testcase_id
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Creates testcase on Allure Testops'
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
            key: :project_id,
            description: 'Project identifier in Allure Testops'
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
