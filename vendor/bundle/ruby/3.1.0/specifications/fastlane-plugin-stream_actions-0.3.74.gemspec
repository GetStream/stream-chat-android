# -*- encoding: utf-8 -*-
# stub: fastlane-plugin-stream_actions 0.3.74 ruby lib

Gem::Specification.new do |s|
  s.name = "fastlane-plugin-stream_actions".freeze
  s.version = "0.3.74"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.metadata = { "rubygems_mfa_required" => "true" } if s.respond_to? :metadata=
  s.require_paths = ["lib".freeze]
  s.authors = ["GetStream".freeze]
  s.date = "2024-11-13"
  s.email = "alexey.alterpesotskiy@getstream.io".freeze
  s.homepage = "https://github.com/GetStream/fastlane-plugin-stream_actions".freeze
  s.required_ruby_version = Gem::Requirement.new(">= 2.4".freeze)
  s.rubygems_version = "3.3.27".freeze
  s.summary = "stream custom actions".freeze

  s.installed_by_version = "3.3.27" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_runtime_dependency(%q<xctest_list>.freeze, ["= 1.2.1"])
    s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_development_dependency(%q<fasterer>.freeze, ["= 0.9.0"])
    s.add_development_dependency(%q<fastlane>.freeze, [">= 2.182.0"])
    s.add_development_dependency(%q<plist>.freeze, [">= 0"])
    s.add_development_dependency(%q<pry>.freeze, [">= 0"])
    s.add_development_dependency(%q<rake>.freeze, [">= 0"])
    s.add_development_dependency(%q<rspec>.freeze, [">= 0"])
    s.add_development_dependency(%q<rspec_junit_formatter>.freeze, [">= 0"])
    s.add_development_dependency(%q<rubocop>.freeze, ["= 1.38"])
    s.add_development_dependency(%q<rubocop-performance>.freeze, [">= 0"])
    s.add_development_dependency(%q<rubocop-rake>.freeze, ["= 0.6.0"])
    s.add_development_dependency(%q<rubocop-require_tools>.freeze, [">= 0"])
    s.add_development_dependency(%q<rubocop-rspec>.freeze, ["= 2.15.0"])
    s.add_development_dependency(%q<simplecov>.freeze, [">= 0"])
  else
    s.add_dependency(%q<xctest_list>.freeze, ["= 1.2.1"])
    s.add_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_dependency(%q<fasterer>.freeze, ["= 0.9.0"])
    s.add_dependency(%q<fastlane>.freeze, [">= 2.182.0"])
    s.add_dependency(%q<plist>.freeze, [">= 0"])
    s.add_dependency(%q<pry>.freeze, [">= 0"])
    s.add_dependency(%q<rake>.freeze, [">= 0"])
    s.add_dependency(%q<rspec>.freeze, [">= 0"])
    s.add_dependency(%q<rspec_junit_formatter>.freeze, [">= 0"])
    s.add_dependency(%q<rubocop>.freeze, ["= 1.38"])
    s.add_dependency(%q<rubocop-performance>.freeze, [">= 0"])
    s.add_dependency(%q<rubocop-rake>.freeze, ["= 0.6.0"])
    s.add_dependency(%q<rubocop-require_tools>.freeze, [">= 0"])
    s.add_dependency(%q<rubocop-rspec>.freeze, ["= 2.15.0"])
    s.add_dependency(%q<simplecov>.freeze, [">= 0"])
  end
end
