require_relative '../client'
require_relative '../tunes/display_family'
require_relative 'utilities'

module Spaceship
  # This class is used to upload Digital files (Images, Videos, JSON files) onto the du-itc service.
  # Its implementation is tied to the tunes module (in particular using +AppVersion+ instances)
  class DUClient < Spaceship::Client #:nodoc:
    #####################################################
    # @!group Init and Login
    #####################################################

    def self.hostname
      "https://du-itc.itunes.apple.com"
    end

    #####################################################
    # @!group Images
    #####################################################

    def upload_screenshot(app_version, upload_file, content_provider_id, sso_token_for_image, device, is_messages)
      upload_file(app_version: app_version, upload_file: upload_file, path: '/upload/image', content_provider_id: content_provider_id, sso_token: sso_token_for_image, du_validation_rule_set: screenshot_picture_type(device, is_messages))
    end

    def upload_purchase_merch_screenshot(app_id, upload_file, content_provider_id, sso_token_for_image)
      upload_file(app_id: app_id, upload_file: upload_file, path: '/upload/image', content_provider_id: content_provider_id, sso_token: sso_token_for_image, du_validation_rule_set: 'MZPFT.MerchandisingIAPIcon')
    end

    def upload_purchase_review_screenshot(app_id, upload_file, content_provider_id, sso_token_for_image)
      upload_file(app_id: app_id, upload_file: upload_file, path: '/upload/image', content_provider_id: content_provider_id, sso_token: sso_token_for_image, du_validation_rule_set: get_picture_type(upload_file))
    end

    def upload_large_icon(app_version, upload_file, content_provider_id, sso_token_for_image)
      upload_file(app_version: app_version, upload_file: upload_file, path: '/upload/image', content_provider_id: content_provider_id, sso_token: sso_token_for_image, du_validation_rule_set: 'MZPFT.LargeApplicationIcon')
    end

    def upload_watch_icon(app_version, upload_file, content_provider_id, sso_token_for_image)
      upload_file(app_version: app_version, upload_file: upload_file, path: '/upload/image', content_provider_id: content_provider_id, sso_token: sso_token_for_image, du_validation_rule_set: 'MZPFT.GizmoAppIcon')
    end

    def upload_geojson(app_version, upload_file, content_provider_id, sso_token_for_image)
      upload_file(app_version: app_version, upload_file: upload_file, path: '/upload/geo-json', content_provider_id: content_provider_id, sso_token: sso_token_for_image)
    end

    def upload_trailer(app_version, upload_file, content_provider_id, sso_token_for_video)
      upload_file(app_version: app_version, upload_file: upload_file, path: '/upload/purple-video', content_provider_id: content_provider_id, sso_token: sso_token_for_video)
    end

    def upload_trailer_preview(app_version, upload_file, content_provider_id, sso_token_for_image, device)
      upload_file(app_version: app_version, upload_file: upload_file, path: '/upload/image', content_provider_id: content_provider_id, sso_token: sso_token_for_image, du_validation_rule_set: screenshot_picture_type(device, nil))
    end

    def upload_app_review_attachment(app_version, upload_file, content_provider_id, sso_token_for_attachment)
      upload_file(app_version: app_version, upload_file: upload_file, path: '/upload/app-resolution-file', content_provider_id: content_provider_id, sso_token: sso_token_for_attachment)
    end

    def get_picture_type(upload_file)
      resolution = Utilities.resolution(upload_file.file_path)
      result = device_resolution_map.find do |key, resolutions|
        resolutions.include?(resolution)
      end
      raise "Unknown device for screen resolution #{resolution}" if result.nil?

      picture_type_map[result[0]]
    end

    private

    def upload_file(app_version: nil, upload_file: nil, path: nil, content_provider_id: nil, sso_token: nil, du_validation_rule_set: nil, app_id: nil)
      raise "File #{upload_file.file_path} is empty" if upload_file.file_size == 0

      if app_id
        app_id = app_id
        app_type = nil
        version = nil
        referrer = nil
      else
        version = app_version.version
        app_id = app_version.application.apple_id
        app_type = app_version.app_type
        referrer = app_version.application.url
      end

      r = request(:post) do |req|
        req.url("#{self.class.hostname}#{path}")
        req.body = upload_file.bytes
        req.headers['Accept'] = 'application/json, text/plain, */*'
        req.headers['Content-Type'] = upload_file.content_type
        req.headers['X-Apple-Upload-Referrer'] = referrer if referrer
        req.headers['Referrer'] = referrer if referrer
        req.headers['X-Apple-Upload-AppleId'] = app_id
        req.headers['X-Apple-Jingle-Correlation-Key'] = "#{app_type}:AdamId=#{app_id}:Version=#{version}" if app_type
        req.headers['X-Apple-Upload-itctoken'] = sso_token
        req.headers['X-Apple-Upload-ContentProviderId'] = content_provider_id
        req.headers['X-Original-Filename'] = upload_file.file_name
        req.headers['X-Apple-Upload-Validation-RuleSets'] = du_validation_rule_set if du_validation_rule_set
        req.headers['Content-Length'] = upload_file.file_size.to_s
        req.headers['Connection'] = "keep-alive"
      end

      if r.status == 500 && r.body.include?("Server Error")
        return upload_file(app_version: app_version, upload_file: upload_file, path: path, content_provider_id: content_provider_id, sso_token: sso_token, du_validation_rule_set: du_validation_rule_set, app_id: app_id)
      end

      parse_upload_response(r)
    end

    def picture_type_map
      Spaceship::Tunes::DisplayFamily.all.map { |v| [v.name.to_sym, v.picture_type] }.to_h
    end

    def messages_picture_type_map
      Spaceship::Tunes::DisplayFamily.all.select(&:messages_supported?).map { |v| [v.name.to_sym, v.messages_picture_type] }.to_h
    end

    def device_resolution_map
      Spaceship::Tunes::DisplayFamily.all.map { |v| [v.name.to_sym, v.screenshot_resolutions] }.to_h
    end

    def screenshot_picture_type(device, is_messages)
      map = is_messages ? messages_picture_type_map : picture_type_map
      device = device.to_sym
      raise "Unknown picture type for device: #{device}" unless map.key?(device)
      map[device]
    end

    def parse_upload_response(response)
      content = response.body
      if !content['statusCode'].nil? && content['statusCode'] != 200
        error_codes = ""
        error_codes = content['errorCodes'].join(',') unless content['errorCodes'].nil?
        error_message = "[#{error_codes}] #{content['localizedMessage']}"
        raise UnexpectedResponse.new, error_message
      end
      content
    end
  end
end
