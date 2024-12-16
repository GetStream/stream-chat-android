# frozen_string_literal: true
require_relative 'template'
require 'commonmarker'

if defined?(::Commonmarker)
  aliases = {
    :smartypants => :smart
  }.freeze
  parse_opts = [
    :smart,
    :default_info_string,
  ].freeze
  render_opts = [
    :hardbreaks,
    :github_pre_lang,
    :width,
    :unsafe,
    :escape,
    :sourcepos,
  ].freeze
  exts = [
    :strikethrough,
    :tagfilter,
    :table,
    :autolink,
    :tasklist,
    :superscript,
    :header_ids,
    :footnotes,
    :description_lists,
    :front_matter_delimiter,
    :shortcodes,
  ].freeze

  Tilt::CommonMarkerTemplate = Tilt::StaticTemplate.subclass do
    parse_options = @options.select { |key, _| parse_opts.include?(key.downcase) }.transform_keys(&:downcase)
    parse_options.merge!(@options.select { |key, _| aliases.has_key?(key) }.transform_keys { |key| aliases[key] })
    render_options = @options.select { |key, _| render_opts.include?(key.downcase) }.transform_keys(&:downcase)
    extensions = @options.select { |key, _| exts.include?(key) }.transform_keys(&:downcase)

    Commonmarker.to_html(@data, options: { parse: parse_options, render: render_options, extension: extensions })
  end
# :nocov:
else
  aliases = {
    :smartypants => :SMART
  }.freeze
  parse_opts = [
    :FOOTNOTES,
    :LIBERAL_HTML_TAG,
    :SMART,
    :smartypants,
    :STRIKETHROUGH_DOUBLE_TILDE,
    :UNSAFE,
    :VALIDATE_UTF8,
  ].freeze
  render_opts = [
    :FOOTNOTES,
    :FULL_INFO_STRING,
    :GITHUB_PRE_LANG,
    :HARDBREAKS,
    :NOBREAKS,
    :SAFE, # Removed in v0.18.0 (2018-10-17)
    :SOURCEPOS,
    :TABLE_PREFER_STYLE_ATTRIBUTES,
    :UNSAFE,
  ].freeze
  exts = [
    :autolink,
    :strikethrough,
    :table,
    :tagfilter,
    :tasklist,
  ].freeze

  Tilt::CommonMarkerTemplate = Tilt::StaticTemplate.subclass do
    extensions = exts.select do |extension|
      @options[extension]
    end

    parse_options, render_options = [parse_opts, render_opts].map do |opts|
      opts = opts.select do |option|
        @options[option]
      end.map! do |option|
        aliases[option] || option
      end

      opts = :DEFAULT unless opts.any?
      opts
    end

    CommonMarker.render_doc(@data, parse_options, extensions).to_html(render_options, extensions)
  end
end
# :nocov:
