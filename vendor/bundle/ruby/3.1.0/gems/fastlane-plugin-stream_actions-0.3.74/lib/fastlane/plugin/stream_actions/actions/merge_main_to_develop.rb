module Fastlane
  module Actions
    class MergeMainToDevelopAction < Action
      def self.run(params)
        other_action.ensure_git_status_clean
        sh('git config pull.ff only')
        sh('git fetch --all --tags --prune')
        sh('git checkout main')
        sh('git pull origin main --ff-only')
        sh('git checkout develop')
        sh('git pull origin develop --ff-only')
        sh('git merge main')
        sh('git push origin develop')
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Merge main branch to develop'
      end

      def self.available_options
        []
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
