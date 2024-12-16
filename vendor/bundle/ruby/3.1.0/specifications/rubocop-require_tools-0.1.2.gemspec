# -*- encoding: utf-8 -*-
# stub: rubocop-require_tools 0.1.2 ruby lib

Gem::Specification.new do |s|
  s.name = "rubocop-require_tools".freeze
  s.version = "0.1.2"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Manu Wallner".freeze]
  s.bindir = "exe".freeze
  s.date = "2018-01-08"
  s.description = "Available Cops in this extension:\n - MissingRequireStatement:\n  Missing require statements can cause implicit dependencies in your projects that are error prone and hard to maintain.\n".freeze
  s.email = ["manu@supermil.ch".freeze]
  s.homepage = "https://github.com/milch/rubocop-require_tools".freeze
  s.licenses = ["MIT".freeze]
  s.rubygems_version = "3.3.27".freeze
  s.summary = "Checks require statements in your code".freeze

  s.installed_by_version = "3.3.27" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_runtime_dependency(%q<rubocop>.freeze, [">= 0.49.1"])
    s.add_development_dependency(%q<pry>.freeze, [">= 0"])
    s.add_development_dependency(%q<bundler>.freeze, ["~> 1.16"])
    s.add_development_dependency(%q<rake>.freeze, ["~> 10.0"])
    s.add_development_dependency(%q<rspec>.freeze, ["~> 3.0"])
  else
    s.add_dependency(%q<rubocop>.freeze, [">= 0.49.1"])
    s.add_dependency(%q<pry>.freeze, [">= 0"])
    s.add_dependency(%q<bundler>.freeze, ["~> 1.16"])
    s.add_dependency(%q<rake>.freeze, ["~> 10.0"])
    s.add_dependency(%q<rspec>.freeze, ["~> 3.0"])
  end
end
