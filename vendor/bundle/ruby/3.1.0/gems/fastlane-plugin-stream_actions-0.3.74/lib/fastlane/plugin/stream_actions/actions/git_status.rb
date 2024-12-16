module Fastlane
  module Actions
    class GitStatusAction < Action
      def self.run(params)
        UI.user_error!('Extension should be provided') unless params[:ext]

        untracked_files = sh('git status -s').split("\n").map(&:strip)
        UI.important("Git Status: #{untracked_files}")

        deleted_files = select_files_from(files: untracked_files, with_extension: params[:ext], that_start_with: 'D')
        added_files = select_files_from(files: untracked_files, with_extension: params[:ext], that_start_with: ['A', '??'])
        renamed_files = select_files_from(files: untracked_files, with_extension: params[:ext], that_start_with: 'R')
        modified_files = select_files_from(files: untracked_files, with_extension: params[:ext], that_start_with: 'M')

        renamed_files.each do |renamed_file|
          content = renamed_file.split.drop(1).join.split('->').map(&:strip)
          deleted_files << content.first
          added_files << content.last
        end
        { a: added_files, d: deleted_files, m: modified_files }
      end

      def self.select_files_from(files:, with_extension:, that_start_with:)
        files.select do |f|
          f.start_with?(*that_start_with)
        end.map do |f|
          f.split.drop(1).join(' ')
        end.select do |f|
          f.gsub(/['"]/, '').end_with?(with_extension)
        end
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Git status'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :ext,
            description: 'Extension'
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
