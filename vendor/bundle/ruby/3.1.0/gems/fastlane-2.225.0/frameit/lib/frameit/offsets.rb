require_relative 'module'
require_relative 'frame_downloader'

module Frameit
  class Offsets
    # Returns the image offset needed for a certain device type
    def self.image_offset(screenshot)
      require 'json'

      unless @offsets_cache
        offsets_json_path = File.join(FrameDownloader.new.templates_path, "offsets.json")
        UI.user_error!("Could not find offsets.json file at path '#{offsets_json_path}'") unless File.exist?(offsets_json_path)
        @offsets_cache = JSON.parse(File.read(offsets_json_path))
      end

      offset_value = @offsets_cache["portrait"][sanitize_device_name(screenshot.device_name)]
      UI.error("Tried looking for offset information for 'portrait', #{screenshot.device_name} in '#{offsets_json_path}'") unless offset_value
      return offset_value
    end

    def self.sanitize_device_name(basename)
      # this should be the same as frames_generator's sanitize_device_name (except stripping colors):
      basename = basename.gsub("Apple", "")
      basename = basename.gsub("-", " ")
      basename.strip.to_s
    end
  end
end
