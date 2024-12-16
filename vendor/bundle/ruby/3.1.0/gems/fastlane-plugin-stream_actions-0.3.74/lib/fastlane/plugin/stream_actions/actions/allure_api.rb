module Fastlane
  module Actions
    class AllureApiAction < Action
      def self.run(params)
        url = URI("#{params[:url]}/#{params[:path]}")
        request =
          case params[:http_method].upcase
          when 'GET'
            Net::HTTP::Get.new(url)
          when 'POST'
            Net::HTTP::Post.new(url)
          when 'PATCH'
            Net::HTTP::Patch.new(url)
          when 'DELETE'
            Net::HTTP::Delete.new(url)
          end
        http = Net::HTTP.new(url.host, url.port)
        http.use_ssl = true
        http.verify_mode = OpenSSL::SSL::VERIFY_NONE

        request['authorization'] = "Api-Token #{params[:token]}"
        request['content-type'] = 'application/json'
        request.body = params[:request_body] if params[:request_body]

        response_body = http.request(request).read_body
        JSON.parse(response_body) if !response_body.nil? && !response_body.empty?
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Allure Testops API'
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
            key: :path,
            description: 'Allure Testops endpoint path'
          ),
          FastlaneCore::ConfigItem.new(
            key: :request_body,
            description: 'Request body',
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :http_method,
            description: 'HTTP request method',
            verify_block: proc do |method|
              unless ['GET', 'POST', 'PATCH', 'DELETE'].include?(method)
                UI.user_error!('Path to the Xcode project has to be specified')
              end
            end
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
