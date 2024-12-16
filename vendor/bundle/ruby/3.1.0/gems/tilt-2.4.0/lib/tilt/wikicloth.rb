# frozen_string_literal: true
require_relative 'template'
require 'wikicloth'

warn 'tilt/wikicloth is deprecated, as wikicloth requires modifying string literals', uplevel: 1

# WikiCloth implementation. See: https://github.com/nricciar/wikicloth
Tilt::WikiClothTemplate = Tilt::StaticTemplate.subclass do
  parser = @options.delete(:parser) || WikiCloth::Parser
  @options[:data] = @data
  parser.new(@options).to_html
end
