require 'pathname'

module Fastlane
  module Actions
    module SharedValues
      MODIFIED_FILES = :MODIFIED_FILES
    end

    class << self
      # Add an array of paths relative to the repo root or absolute paths that have been modified by
      # an action.
      #
      # :files: An array of paths relative to the repo root or absolute paths
      def add_modified_files(files)
        modified_files = lane_context[SharedValues::MODIFIED_FILES] || Set.new
        modified_files += files
        lane_context[SharedValues::MODIFIED_FILES] = modified_files
      end
    end

    # Commits the current changes in the repo as a version bump, checking to make sure only files which contain version information have been changed.
    class CommitVersionBumpAction < Action
      def self.run(params)
        require 'xcodeproj'
        require 'set'
        require 'shellwords'

        xcodeproj_path = params[:xcodeproj] ? File.expand_path(File.join('.', params[:xcodeproj])) : nil

        # find the repo root path
        repo_path = Actions.sh('git rev-parse --show-toplevel').strip
        repo_pathname = Pathname.new(repo_path)

        if xcodeproj_path
          # ensure that the xcodeproj passed in was OK
          UI.user_error!("Could not find the specified xcodeproj: #{xcodeproj_path}") unless File.directory?(xcodeproj_path)
        else
          # find an xcodeproj (ignoring dependencies)
          xcodeproj_paths = Fastlane::Helper::XcodeprojHelper.find(repo_path)

          # no projects found: error
          UI.user_error!('Could not find a .xcodeproj in the current repository\'s working directory.') if xcodeproj_paths.count == 0

          # too many projects found: error
          if xcodeproj_paths.count > 1
            relative_projects = xcodeproj_paths.map { |e| Pathname.new(e).relative_path_from(repo_pathname).to_s }.join("\n")
            UI.user_error!("Found multiple .xcodeproj projects in the current repository's working directory. Please specify your app's main project: \n#{relative_projects}")
          end

          # one project found: great
          xcodeproj_path = xcodeproj_paths.first
        end

        # find the pbxproj path, relative to git directory
        pbxproj_pathname = Pathname.new(File.join(xcodeproj_path, 'project.pbxproj'))
        pbxproj_path = pbxproj_pathname.relative_path_from(repo_pathname).to_s

        # find the info_plist files
        project = Xcodeproj::Project.open(xcodeproj_path)
        info_plist_files = project.objects.select do |object|
          object.isa == 'XCBuildConfiguration'
        end.map(&:to_hash).map do |object_hash|
          object_hash['buildSettings']
        end.select do |build_settings|
          build_settings.key?('INFOPLIST_FILE')
        end.map do |build_settings|
          build_settings['INFOPLIST_FILE']
        end.uniq.map do |info_plist_path|
          Pathname.new(File.expand_path(File.join(xcodeproj_path, '..', info_plist_path))).relative_path_from(repo_pathname).to_s
        end

        # Removes .plist files that matched the given expression in the 'ignore' parameter
        ignore_expression = params[:ignore]
        if ignore_expression
          info_plist_files.reject! do |info_plist_file|
            info_plist_file.match(ignore_expression)
          end
        end

        extra_files = params[:include]
        extra_files += modified_files_relative_to_repo_root(repo_path)

        # create our list of files that we expect to have changed, they should all be relative to the project root, which should be equal to the git workdir root
        expected_changed_files = extra_files
        expected_changed_files << pbxproj_path
        expected_changed_files << info_plist_files

        if params[:settings]
          settings_plists_from_param(params[:settings]).each do |file|
            settings_file_pathname = Pathname.new(settings_bundle_file_path(project, file))
            expected_changed_files << settings_file_pathname.relative_path_from(repo_pathname).to_s
          end
        end

        expected_changed_files.flatten!.uniq!

        # get the list of files that have actually changed in our git workdir
        git_dirty_files = Actions.sh('git diff --name-only HEAD').split("\n") + Actions.sh('git ls-files --other --exclude-standard').split("\n")

        # little user hint
        UI.user_error!("No file changes picked up. Make sure you run the `increment_build_number` action first.") if git_dirty_files.empty?

        # check if the files changed are the ones we expected to change (these should be only the files that have version info in them)
        changed_files_as_expected = Set.new(git_dirty_files.map(&:downcase)).subset?(Set.new(expected_changed_files.map(&:downcase)))
        unless changed_files_as_expected
          unless params[:force]
            error = [
              "Found unexpected uncommitted changes in the working directory. Expected these files to have ",
              "changed: \n#{expected_changed_files.join("\n")}.\nBut found these actual changes: ",
              "#{git_dirty_files.join("\n")}.\nMake sure you have cleaned up the build artifacts and ",
              "are only left with the changed version files at this stage in your lane, and don't touch the ",
              "working directory while your lane is running. You can also use the :force option to bypass this ",
              "check, and always commit a version bump regardless of the state of the working directory."
            ].join("\n")
            UI.user_error!(error)
          end
        end

        # get the absolute paths to the files
        git_add_paths = expected_changed_files.map do |path|
          updated = path.gsub("$(SRCROOT)", ".").gsub("${SRCROOT}", ".")
          File.expand_path(File.join(repo_pathname, updated))
        end

        # then create a commit with a message
        Actions.sh("git add #{git_add_paths.map(&:shellescape).join(' ')}")

        begin
          command = build_git_command(params)

          Actions.sh(command)

          UI.success("Committed \"#{params[:message]}\" 💾.")
        rescue => ex
          UI.error(ex)
          UI.important("Didn't commit any changes.")
        end
      end

      def self.description
        "Creates a 'Version Bump' commit. Run after `increment_build_number`"
      end

      def self.output
        [
          ['MODIFIED_FILES', 'The list of paths of modified files']
        ]
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(key: :message,
                                       env_name: "FL_COMMIT_BUMP_MESSAGE",
                                       description: "The commit message when committing the version bump",
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :xcodeproj,
                                       env_name: "FL_BUILD_NUMBER_PROJECT",
                                       description: "The path to your project file (Not the workspace). If you have only one, this is optional",
                                       optional: true,
                                       verify_block: proc do |value|
                                         UI.user_error!("Please pass the path to the project, not the workspace") if value.end_with?(".xcworkspace")
                                         UI.user_error!("Could not find Xcode project") unless File.exist?(value)
                                       end),
          FastlaneCore::ConfigItem.new(key: :force,
                                       env_name: "FL_FORCE_COMMIT",
                                       description: "Forces the commit, even if other files than the ones containing the version number have been modified",
                                       type: Boolean,
                                       optional: true,
                                       default_value: false),
          FastlaneCore::ConfigItem.new(key: :settings,
                                       env_name: "FL_COMMIT_INCLUDE_SETTINGS",
                                       description: "Include Settings.bundle/Root.plist with version bump",
                                       skip_type_validation: true, # allows Boolean, String, Array
                                       optional: true,
                                       default_value: false),
          FastlaneCore::ConfigItem.new(key: :ignore,
                                       description: "A regular expression used to filter matched plist files to be modified",
                                       skip_type_validation: true, # allows Regex
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :include,
                                       description: "A list of extra files to be included in the version bump (string array or comma-separated string)",
                                       optional: true,
                                       default_value: [],
                                       type: Array),
          FastlaneCore::ConfigItem.new(key: :no_verify,
                                      env_name: "FL_GIT_PUSH_USE_NO_VERIFY",
                                      description: "Whether or not to use --no-verify",
                                      type: Boolean,
                                      default_value: false)
        ]
      end

      def self.details
        list = <<-LIST.markdown_list
          All `.plist` files
          The `.xcodeproj/project.pbxproj` file
        LIST

        [
          "This action will create a 'Version Bump' commit in your repo. Useful in conjunction with `increment_build_number`.",
          "It checks the repo to make sure that only the relevant files have changed. These are the files that `increment_build_number` (`agvtool`) touches:".markdown_preserve_newlines,
          list,
          "Then commits those files to the repo.",
          "Customize the message with the `:message` option. It defaults to 'Version Bump'.",
          "If you have other uncommitted changes in your repo, this action will fail. If you started off in a clean repo, and used the _ipa_ and or _sigh_ actions, then you can use the [clean_build_artifacts](https://docs.fastlane.tools/actions/clean_build_artifacts/) action to clean those temporary files up before running this action."
        ].join("\n")
      end

      def self.author
        "lmirosevic"
      end

      def self.is_supported?(platform)
        [:ios, :mac].include?(platform)
      end

      def self.example_code
        [
          'commit_version_bump',
          'commit_version_bump(
            message: "Version Bump",                    # create a commit with a custom message
            xcodeproj: "./path/to/MyProject.xcodeproj"  # optional, if you have multiple Xcode project files, you must specify your main project here
          )',
          'commit_version_bump(
            settings: true # Include Settings.bundle/Root.plist
          )',
          'commit_version_bump(
            settings: "About.plist" # Include Settings.bundle/About.plist
          )',
          'commit_version_bump(
            settings: %w[About.plist Root.plist] # Include more than one plist from Settings.bundle
          )',
          'commit_version_bump(
            include: %w[package.json custom.cfg] # include other updated files as part of the version bump
          )',
          'commit_version_bump(
            ignore: /OtherProject/ # ignore files matching a regular expression
          )',
          'commit_version_bump(
            no_verify: true           # optional, default: false
          )'
        ]
      end

      def self.category
        :source_control
      end

      class << self
        def settings_plists_from_param(param)
          if param.kind_of?(String)
            # commit_version_bump settings: "About.plist"
            return [param]
          elsif param.kind_of?(Array)
            # commit_version_bump settings: ["Root.plist", "About.plist"]
            return param
          else
            # commit_version_bump settings: true # Root.plist
            return ["Root.plist"]
          end
        end

        def settings_bundle_file_path(project, settings_file_name)
          settings_bundle = project.files.find { |f| f.path =~ /Settings.bundle/ }
          raise "No Settings.bundle in project" if settings_bundle.nil?

          return File.join(settings_bundle.real_path, settings_file_name)
        end

        def modified_files_relative_to_repo_root(repo_root)
          return [] if Actions.lane_context[SharedValues::MODIFIED_FILES].nil?

          root_pathname = Pathname.new(repo_root)
          all_modified_files = Actions.lane_context[SharedValues::MODIFIED_FILES].map do |path|
            next path unless path =~ %r{^/}
            Pathname.new(path).relative_path_from(root_pathname).to_s
          end
          return all_modified_files.uniq
        end

        def build_git_command(params)
          build_number = Actions.lane_context[Actions::SharedValues::BUILD_NUMBER]

          params[:message] ||= (build_number ? "Version Bump to #{build_number}" : "Version Bump")

          command = [
            'git',
            'commit',
            '-m',
            "'#{params[:message]}'"
          ]

          command << '--no-verify' if params[:no_verify]

          return command.join(' ')
        end
      end
    end
  end
end
