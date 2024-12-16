require 'fastlane/swift_fastlane_function.rb'

module Fastlane
  class SwiftToolDetail
    attr_accessor :swift_class
    attr_accessor :swift_protocol
    attr_accessor :command_line_tool_name

    def initialize(command_line_tool_name: nil, swift_class: nil, swift_protocol: nil)
      self.command_line_tool_name = command_line_tool_name
      self.swift_class = swift_class
      self.swift_protocol = swift_protocol
    end
  end

  class SwiftAPIGenerator
  end

  class SwiftFastlaneAPIGenerator < SwiftAPIGenerator
    def initialize(target_output_path: "swift")
      @target_filename = "Fastlane.swift"
      @target_output_path = File.expand_path(target_output_path)
      @generated_paths = []

      super()

      self.actions_not_supported = ["import", "import_from_git"].to_set
      self.action_options_to_ignore = {

        "precheck" => [
          "negative_apple_sentiment",
          "placeholder_text",
          "other_platforms",
          "future_functionality",
          "test_words",
          "curse_words",
          "custom_text",
          "copyright_date",
          "unreachable_urls"
        ].to_set
      }
    end

    def extend_content(file_content, tool_details)
      file_content << "" # newline because we're adding an extension
      file_content << "// These are all the parsing functions needed to transform our data into the expected types"
      file_content << generate_lanefile_parsing_functions

      tool_objects = generate_lanefile_tool_objects(classes: tool_details.map(&:swift_class))
      file_content << tool_objects

      old_file_content = File.read(fastlane_swift_api_path)
      new_file_content = file_content.join("\n")

      # compare old file content to potential new file content
      api_version = determine_api_version(new_file_content: new_file_content, old_file_content: old_file_content)
      old_api_version = find_api_version_string(content: old_file_content)

      # if there is a change, we need to write out the new file
      if api_version != old_api_version
        file_content << autogen_version_warning_text(api_version: api_version)
      else
        file_content = nil
      end

      return file_content
    end
  end

  class SwiftActionsAPIGenerator < SwiftAPIGenerator
    def initialize(target_output_path: "swift")
      @target_filename = "Actions.swift"
      @target_output_path = File.expand_path(target_output_path)
      @generated_paths = []

      super()

      # Excludes all actions that aren't external actions (including plugins)
      available_external_actions = Fastlane.external_actions || []
      available_actions = []
      ActionsList.all_actions do |action|
        next unless action.respond_to?(:action_name)
        available_actions << action.action_name unless available_external_actions.include?(action)
      end

      self.actions_not_supported = (["import", "import_from_git"] + available_actions).to_set
      self.action_options_to_ignore = {}
    end
  end

  class SwiftPluginsAPIGenerator < SwiftAPIGenerator
    def initialize(target_output_path: "swift")
      @target_filename = "Plugins.swift"
      @target_output_path = File.expand_path(target_output_path)
      @generated_paths = []

      super()

      # Gets list of plugin actions
      plugin_actions = Fastlane.plugin_manager.plugin_references.values.flat_map do |info|
        info[:actions]
      end

      # Action references from plugins
      available_plugins = plugin_actions.map do |plugin_action|
        Fastlane::Runner.new.class_reference_from_action_name(plugin_action)
      end

      # Excludes all actions that aren't pluign actions (including external actions)
      available_actions = []
      ActionsList.all_actions do |action|
        next unless action.respond_to?(:action_name)
        available_actions << action.action_name unless available_plugins.include?(action)
      end

      self.actions_not_supported = (["import", "import_from_git"] + available_actions).to_set
      self.action_options_to_ignore = {}
    end
  end

  class SwiftAPIGenerator
    DEFAULT_API_VERSION_STRING = "0.9.1"
    attr_accessor :tools_option_files
    attr_accessor :actions_not_supported
    attr_accessor :action_options_to_ignore
    attr_accessor :target_output_path
    attr_accessor :target_filename
    attr_accessor :generated_paths # stores all file names of generated files (as they are generated)

    attr_accessor :fastlane_swift_api_path

    def initialize
      require 'fastlane'
      require 'fastlane/documentation/actions_list'
      Fastlane.load_actions
      # Tools that can be used with <Toolname>file, like Deliverfile, Screengrabfile
      # this is important because we need to generate the proper api for these by creating a protocol
      # with default implementation we can use in the Fastlane.swift API if people want to use
      # <Toolname>file.swift files.
      self.tools_option_files = TOOL_CONFIG_FILES.map { |config_file| config_file.downcase.chomp("file") }.to_set

      @fastlane_swift_api_path = File.join(@target_output_path, @target_filename)
    end

    def extend_content(content, tool_details)
      return content
    end

    def generate_swift
      self.generated_paths = [] # reset generated paths in case we're called multiple times
      file_content = []
      file_content << "import Foundation"

      tool_details = []
      ActionsList.all_actions do |action|
        next unless action.respond_to?(:action_name)
        next if self.actions_not_supported.include?(action.action_name)

        swift_function = process_action(action: action)
        if defined?(swift_function.class_name)
          tool_details << SwiftToolDetail.new(
            command_line_tool_name: action.action_name,
            swift_class: swift_function.class_name,
            swift_protocol: swift_function.protocol_name
          )
        end
        unless swift_function
          next
        end

        file_content << swift_function.swift_code
      end

      file_content = extend_content(file_content, tool_details)

      if file_content
        new_file_content = file_content.join("\n")

        File.write(fastlane_swift_api_path, new_file_content)
        UI.success(fastlane_swift_api_path)
        self.generated_paths << fastlane_swift_api_path
      end

      default_implementations_path = generate_default_implementations(tool_details: tool_details)

      # we might not have any changes, like if it's a hotpatch
      self.generated_paths += default_implementations_path if default_implementations_path.length > 0

      return self.generated_paths
    end

    def write_lanefile(lanefile_implementation_opening: nil, class_name: nil, tool_name: nil)
      disclaimer = []
      disclaimer << "// *bait*" # As we are using a custom common header, we have to bait with a random comment so it does not remove important text.
      disclaimer << ""
      disclaimer << "// This class is automatically included in FastlaneRunner during build"
      disclaimer << ""
      disclaimer << "// This autogenerated file will be overwritten or replaced during build time, or when you initialize `#{tool_name}`"
      disclaimer << lanefile_implementation_opening
      disclaimer << "// If you want to enable `#{tool_name}`, run `fastlane #{tool_name} init`"
      disclaimer << "// After, this file will be replaced with a custom implementation that contains values you supplied"
      disclaimer << "// during the `init` process, and you won't see this message"
      disclaimer << "}"
      disclaimer << ""
      disclaimer << ""
      disclaimer << ""
      disclaimer << ""
      disclaimer << ""
      disclaimer << "// Generated with fastlane #{Fastlane::VERSION}"
      disclaimer << ""

      file_content = disclaimer.join("\n")

      target_path = File.join(@target_output_path, "#{class_name}.swift")
      File.write(target_path, file_content)
      UI.success(target_path)
      return target_path
    end

    def generate_default_implementations(tool_details: nil)
      files_generated = []
      tool_details.each do |tool_detail|
        header = []
        header << "//"
        header << "//  ** NOTE **"
        header << "//  This file is provided by fastlane and WILL be overwritten in future updates"
        header << "//  If you want to add extra functionality to this project, create a new file in a"
        header << "//  new group so that it won't be marked for upgrade"
        header << "//"
        header << ""
        header << "public class #{tool_detail.swift_class}: #{tool_detail.swift_protocol} {"
        lanefile_implementation_opening = header.join("\n")

        files_generated << write_lanefile(
          lanefile_implementation_opening: lanefile_implementation_opening,
          class_name: tool_detail.swift_class,
          tool_name: tool_detail.command_line_tool_name
        )
      end
      return files_generated
    end

    def generate_lanefile_parsing_functions
      parsing_functions = 'func parseArray(fromString: String, function: String = #function) -> [String] {
  verbose(message: "parsing an Array from data: \(fromString), from function: \(function)")
  let potentialArray: String
  if fromString.count < 2 {
    potentialArray = "[\(fromString)]"
  } else {
    potentialArray = fromString
  }
  let array: [String] = try! JSONSerialization.jsonObject(with: potentialArray.data(using: .utf8)!, options: []) as! [String]
  return array
}

func parseDictionary(fromString: String, function: String = #function) -> [String : String] {
    return parseDictionaryHelper(fromString: fromString, function: function) as! [String: String]
}

func parseDictionary(fromString: String, function: String = #function) -> [String : Any] {
    return parseDictionaryHelper(fromString: fromString, function: function)
}

func parseDictionaryHelper(fromString: String, function: String = #function) -> [String : Any] {
  verbose(message: "parsing an Array from data: \(fromString), from function: \(function)")
  let potentialDictionary: String
  if fromString.count < 2 {
    verbose(message: "Dictionary value too small: \(fromString), from function: \(function)")
    potentialDictionary = "{}"
  } else {
      potentialDictionary = fromString
  }
  let dictionary: [String : Any] = try! JSONSerialization.jsonObject(with: potentialDictionary.data(using: .utf8)!, options: []) as! [String : Any]
  return dictionary
}

func parseBool(fromString: String, function: String = #function) -> Bool {
  verbose(message: "parsing a Bool from data: \(fromString), from function: \(function)")
  return NSString(string: fromString.trimmingCharacters(in: .punctuationCharacters)).boolValue
}

func parseInt(fromString: String, function: String = #function) -> Int {
  verbose(message: "parsing an Int from data: \(fromString), from function: \(function)")
  return NSString(string: fromString.trimmingCharacters(in: .punctuationCharacters)).integerValue
}
      '
      return parsing_functions
    end

    def generate_lanefile_tool_objects(classes: nil)
      objects = classes.map do |filename|
        "public let #{filename.downcase}: #{filename} = #{filename}()"
      end
      return objects
    end

    def autogen_version_warning_text(api_version: nil)
      warning_text_array = []
      warning_text_array << ""
      warning_text_array << "// Please don't remove the lines below"
      warning_text_array << "// They are used to detect outdated files"
      warning_text_array << "// FastlaneRunnerAPIVersion [#{api_version}]"
      warning_text_array << ""
      return warning_text_array.join("\n")
    end

    # compares the new file content to the old and figures out what api_version the new content should be
    def determine_api_version(new_file_content: nil, old_file_content: nil)
      # we know 100% there is a difference, so no need to compare
      unless old_file_content.length >= new_file_content.length
        old_api_version = find_api_version_string(content: old_file_content)

        return DEFAULT_API_VERSION_STRING if old_api_version.nil?

        return increment_api_version_string(api_version_string: old_api_version)
      end

      relevant_old_file_content = old_file_content[0..(new_file_content.length - 1)]

      if relevant_old_file_content == new_file_content
        # no changes at all, just return the same old api version string
        return find_api_version_string(content: old_file_content)
      else
        # there are differences, so calculate a new api_version_string
        old_api_version = find_api_version_string(content: old_file_content)

        return DEFAULT_API_VERSION_STRING if old_api_version.nil?

        return increment_api_version_string(api_version_string: old_api_version)
      end
    end

    # expects format to be "X.Y.Z" where each value is a number
    def increment_api_version_string(api_version_string: nil, increment_by: :patch)
      versions = api_version_string.split(".")
      major = versions[0].to_i
      minor = versions[1].to_i
      patch = versions[2].to_i

      case increment_by
      when :patch
        patch += 1
      when :minor
        minor += 1
        patch = 0
      when :major
        major += 1
        minor = 0
        patch = 0
      end

      new_version_string = [major, minor, patch].join(".")
      return new_version_string
    end

    def find_api_version_string(content: nil)
      regex = SwiftRunnerUpgrader::API_VERSION_REGEX
      matches = content.match(regex)
      if matches.length > 0
        return matches[1]
      else
        return nil
      end
    end

    def generate_tool_protocol(tool_swift_function: nil)
      protocol_content_array = []
      protocol_name = tool_swift_function.protocol_name

      protocol_content_array << "public protocol #{protocol_name}: AnyObject {"
      protocol_content_array += tool_swift_function.swift_vars
      protocol_content_array << "}"
      protocol_content_array << ""

      protocol_content_array << "public extension #{protocol_name} {"
      protocol_content_array += tool_swift_function.swift_default_implementations
      protocol_content_array << "}"
      protocol_content_array << ""
      new_file_content = protocol_content_array.join("\n")

      target_path = File.join(@target_output_path, "#{protocol_name}.swift")

      old_file_content = ""
      # we might have a new file here, unlikely, but possible
      if File.exist?(target_path)
        old_file_content = File.read(target_path)
      end

      # compare old file content to potential new file content
      api_version = determine_api_version(new_file_content: new_file_content, old_file_content: old_file_content)
      old_api_version = find_api_version_string(content: old_file_content)

      # we don't need to write this file out because the file versions are exactly the same
      return nil if api_version == old_api_version

      # use api_version to generate the disclaimer
      api_version_disclaimer = autogen_version_warning_text(api_version: api_version)
      new_file_content.concat(api_version_disclaimer)

      target_path = File.join(@target_output_path, "#{protocol_name}.swift")

      File.write(target_path, new_file_content)
      UI.success(target_path)
      return target_path
    end

    def ignore_param?(function_name: nil, param_name: nil)
      option_set = @action_options_to_ignore[function_name.to_s]
      unless option_set
        return false
      end

      return option_set.include?(param_name.to_s)
    end

    def process_action(action: nil)
      options = action.available_options || []

      action_name = action.action_name
      keys = []
      key_descriptions = []
      key_default_values = []
      key_optionality_values = []
      key_type_overrides = []
      key_is_strings = []

      if options.kind_of?(Array)
        options.each do |current|
          next unless current.kind_of?(FastlaneCore::ConfigItem)

          if ignore_param?(function_name: action_name, param_name: current.key)
            next
          end
          keys << current.key.to_s
          key_descriptions << current.description
          key_default_values << current.code_gen_default_value
          key_optionality_values << current.optional
          key_type_overrides << current.data_type
          key_is_strings << current.is_string
        end
      end
      action_return_type = action.return_type

      if self.tools_option_files.include?(action_name.to_s.downcase)
        tool_swift_function = ToolSwiftFunction.new(
          action_name: action_name,
          action_description: action.description,
          action_details: action.details,
          keys: keys,
          key_descriptions: key_descriptions,
          key_default_values: key_default_values,
          key_optionality_values: key_optionality_values,
          key_type_overrides: key_type_overrides,
          key_is_strings: key_is_strings,
          return_type: action_return_type,
          return_value: action.return_value,
          sample_return_value: action.sample_return_value
        )
        generated_protocol_file_path = generate_tool_protocol(tool_swift_function: tool_swift_function)
        self.generated_paths << generated_protocol_file_path unless generated_protocol_file_path.nil?
        return tool_swift_function
      else
        return SwiftFunction.new(
          action_name: action_name,
          action_description: action.description,
          action_details: action.details,
          keys: keys,
          key_descriptions: key_descriptions,
          key_default_values: key_default_values,
          key_optionality_values: key_optionality_values,
          key_type_overrides: key_type_overrides,
          key_is_strings: key_is_strings,
          return_type: action_return_type,
          return_value: action.return_value,
          sample_return_value: action.sample_return_value
        )
      end
    end
  end
end
