require_relative '../model'
require 'spaceship/test_flight/build'
module Spaceship
  class ConnectAPI
    class Build
      include Spaceship::ConnectAPI::Model

      attr_accessor :version
      attr_accessor :uploaded_date
      attr_accessor :expiration_date
      attr_accessor :expired
      attr_accessor :min_os_version
      attr_accessor :icon_asset_token
      attr_accessor :processing_state
      attr_accessor :uses_non_exempt_encryption

      attr_accessor :app
      attr_accessor :beta_app_review_submission
      attr_accessor :beta_build_metrics
      attr_accessor :beta_build_localizations
      attr_accessor :build_beta_detail
      attr_accessor :build_bundles
      attr_accessor :pre_release_version
      attr_accessor :individual_testers

      attr_mapping({
        "version" => "version",
        "uploadedDate" => "uploaded_date",
        "expirationDate" => "expiration_date",
        "expired" => "expired",
        "minOsVersion" => "min_os_version",
        "iconAssetToken" => "icon_asset_token",
        "processingState" => "processing_state",
        "usesNonExemptEncryption" => "uses_non_exempt_encryption",

        "app" => "app",
        "betaAppReviewSubmission" => "beta_app_review_submission",
        "betaBuildMetrics" => "beta_build_metrics",
        "betaBuildLocalizations" => "beta_build_localizations",
        "buildBetaDetail" => "build_beta_detail",
        "preReleaseVersion" => "pre_release_version",
        "individualTesters" => "individual_testers",
        "buildBundles" => "build_bundles"
      })

      ESSENTIAL_INCLUDES = "app,buildBetaDetail,preReleaseVersion,buildBundles"

      module ProcessingState
        PROCESSING = "PROCESSING"
        FAILED = "FAILED"
        INVALID = "INVALID"
        VALID = "VALID"
      end

      def self.type
        return "builds"
      end

      #
      # Helpers
      #

      def app_version
        raise "No pre_release_version included" unless pre_release_version
        return pre_release_version.version
      end

      def app_id
        raise "No app included" unless app
        return app.id
      end

      def bundle_id
        raise "No app included" unless app
        return app.bundle_id
      end

      def platform
        raise "No pre_release_version included" unless pre_release_version
        return pre_release_version.platform
      end

      def processed?
        return processing_state != ProcessingState::PROCESSING
      end

      def ready_for_internal_testing?
        return build_beta_detail.nil? == false && build_beta_detail.ready_for_internal_testing?
      end

      def ready_for_external_testing?
        return build_beta_detail.nil? == false && build_beta_detail.ready_for_external_testing?
      end

      def ready_for_beta_submission?
        raise "No build_beta_detail included" unless build_beta_detail
        return build_beta_detail.ready_for_beta_submission?
      end

      def missing_export_compliance?
        raise "No build_beta_detail included" unless build_beta_detail
        return build_beta_detail.missing_export_compliance?
      end

      # This is here temporarily until the removal of Spaceship::TestFlight
      def to_testflight_build
        h = {
          'id' => id,
          'buildVersion' => version,
          'uploadDate' => uploaded_date,
          'externalState' => processed? ? Spaceship::TestFlight::Build::BUILD_STATES[:active] : Spaceship::TestFlight::Build::BUILD_STATES[:processing],
          'appAdamId' => app_id,
          'bundleId' => bundle_id,
          'trainVersion' => app_version
        }

        return Spaceship::TestFlight::Build.new(h)
      end

      #
      # API
      #

      def self.all(client: nil, app_id: nil, version: nil, build_number: nil, platform: nil, processing_states: "PROCESSING,FAILED,INVALID,VALID", includes: ESSENTIAL_INCLUDES, sort: "-uploadedDate", limit: 30)
        client ||= Spaceship::ConnectAPI
        resps = client.get_builds(
          filter: { app: app_id, "preReleaseVersion.version" => version, version: build_number, processingState: processing_states },
          includes: includes,
          sort: sort,
          limit: limit
        ).all_pages
        models = resps.flat_map(&:to_models)

        # Filtering after models are fetched since there is no way to do this in a query param filter
        if platform
          models = models.select do |build|
            build.pre_release_version && build.pre_release_version.platform == platform
          end
        end

        return models
      end

      def self.get(client: nil, build_id: nil, includes: ESSENTIAL_INCLUDES)
        client ||= Spaceship::ConnectAPI
        return client.get_build(build_id: build_id, includes: includes).first
      end

      def update(client: nil, attributes: nil)
        client ||= Spaceship::ConnectAPI
        attributes = reverse_attr_mapping(attributes)
        return client.patch_builds(build_id: id, attributes: attributes).first
      end

      def add_beta_groups(client: nil, beta_groups: nil)
        client ||= Spaceship::ConnectAPI
        beta_groups ||= []
        beta_group_ids = beta_groups.map(&:id)
        return client.add_beta_groups_to_build(build_id: id, beta_group_ids: beta_group_ids)
      end

      def get_beta_build_localizations(client: nil, filter: {}, includes: nil, limit: nil, sort: nil)
        client ||= Spaceship::ConnectAPI
        resps = client.get_beta_build_localizations(
          filter: { build: id },
          includes: includes,
          sort: sort,
          limit: limit
        ).all_pages
        return resps.flat_map(&:to_models)
      end

      def get_build_beta_details(client: nil, filter: {}, includes: nil, limit: nil, sort: nil)
        client ||= Spaceship::ConnectAPI
        resps = client.get_build_beta_details(
          filter: { build: id },
          includes: includes,
          sort: sort,
          limit: limit
        ).all_pages
        return resps.flat_map(&:to_models)
      end

      def post_beta_app_review_submission(client: nil)
        client ||= Spaceship::ConnectAPI
        return client.post_beta_app_review_submissions(build_id: id)
      end

      def expire!(client: nil)
        client ||= Spaceship::ConnectAPI
        return client.patch_builds(build_id: id, attributes: { expired: true })
      end
    end
  end
end
