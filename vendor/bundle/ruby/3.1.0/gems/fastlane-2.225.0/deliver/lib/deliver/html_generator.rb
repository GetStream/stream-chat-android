require 'spaceship'

require_relative 'module'

module Deliver
  class HtmlGenerator
    # Splits keywords supporting:
    # * separated by commas (with optional whitespace)
    # * separated by newlines
    KEYWORD_SPLITTER = /(?:,\s?|\r?\n)/

    def run(options, screenshots)
      begin
        # Use fastlane folder or default to current directory
        fastlane_path = FastlaneCore::FastlaneFolder.path || "."
        html_path = self.render(options, screenshots, fastlane_path)
      rescue => ex
        UI.error(ex.inspect)
        UI.error(ex.backtrace.join("\n"))
        okay = UI.input("Could not render HTML preview. Do you still want to continue?")
        return if okay
        UI.crash!("Could not render HTML page")
      end
      UI.important("Verifying the upload via the HTML file can be disabled by either adding")
      UI.important("`force true` to your Deliverfile or using `fastlane deliver --force`")

      system("open '#{html_path}'")
      okay = UI.confirm("Does the Preview on path '#{html_path}' look okay for you?")

      if okay
        UI.success("HTML file confirmed...") # print this to give feedback to the user immediately
      else
        UI.user_error!("Did not upload the metadata, because the HTML file was rejected by the user")
      end
    end

    # Returns a path relative to FastlaneFolder.path
    # This is needed as the Preview.html file is located inside FastlaneFolder.path
    def render_relative_path(export_path, path)
      export_path = Pathname.new(File.expand_path(export_path))
      path = Pathname.new(File.expand_path(path)).relative_path_from(export_path)
      return path.to_path
    end

    # Renders all data available to quickly see if everything was correctly generated.
    # @param export_path (String) The path to a folder where the resulting HTML file should be stored.
    def render(options, screenshots, export_path = nil)
      @screenshots = screenshots || []
      @options = options
      @export_path = export_path

      @app_name = (options[:name]['en-US'] || options[:name].values.first) if options[:name]
      @app_name ||= Deliver.cache[:app].name

      @languages = options[:description].keys if options[:description]
      @languages ||= begin
        platform = Spaceship::ConnectAPI::Platform.map(options[:platform])
        version = Deliver.cache[:app].get_edit_app_store_version(platform: platform)

        version.get_app_store_version_localizations.collect(&:locale)
      end

      html_path = File.join(Deliver::ROOT, "lib/assets/summary.html.erb")
      html = ERB.new(File.read(html_path)).result(binding) # https://web.archive.org/web/20160430190141/www.rrn.dk/rubys-erb-templating-system

      export_path = File.join(export_path, "Preview.html")
      File.write(export_path, html)

      return export_path
    end

    # Splits a string of keywords separated by comma or newlines into a presentable list
    # @param keywords (String)
    def split_keywords(keywords)
      keywords.split(KEYWORD_SPLITTER)
    end
  end
end
