# frozen_string_literal: true
require_relative 'template'
require 'maruku'

warn 'tilt/maruku is deprecated, as maruku requires modifying string literals', uplevel: 1

# Maruku markdown implementation. See: https://github.com/bhollis/maruku
Tilt::MarukuTemplate = Tilt::StaticTemplate.subclass do
  Maruku.new(@data, @options).to_html
end
