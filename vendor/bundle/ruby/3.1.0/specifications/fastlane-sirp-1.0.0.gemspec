# -*- encoding: utf-8 -*-
# stub: fastlane-sirp 1.0.0 ruby lib

Gem::Specification.new do |s|
  s.name = "fastlane-sirp".freeze
  s.version = "1.0.0"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Glenn Rempe".freeze, "lamikae".freeze, "snatchev".freeze, "joshdholtz".freeze]
  s.bindir = "exe".freeze
  s.date = "2024-10-19"
  s.description = "    A Ruby implementation of the Secure Remote Password protocol (SRP-6a).\n    SiRP is a cryptographically strong authentication protocol for\n    password-based, mutual authentication over an insecure network connection.\n".freeze
  s.email = ["me@joshholtz.com".freeze]
  s.homepage = "https://github.com/grempe/sirp".freeze
  s.licenses = ["BSD-3-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.1.0".freeze)
  s.rubygems_version = "3.3.27".freeze
  s.summary = "Secure (interoperable) Remote Password Auth (SRP-6a)".freeze

  s.installed_by_version = "3.3.27" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_runtime_dependency(%q<sysrandom>.freeze, ["~> 1.0"])
    s.add_development_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_development_dependency(%q<rake>.freeze, [">= 0"])
    s.add_development_dependency(%q<rspec>.freeze, ["~> 3.4"])
    s.add_development_dependency(%q<pry>.freeze, ["~> 0.12"])
    s.add_development_dependency(%q<coveralls>.freeze, ["~> 0.8"])
    s.add_development_dependency(%q<coco>.freeze, ["~> 0.15"])
    s.add_development_dependency(%q<wwtd>.freeze, ["~> 1.3"])
  else
    s.add_dependency(%q<sysrandom>.freeze, ["~> 1.0"])
    s.add_dependency(%q<bundler>.freeze, [">= 0"])
    s.add_dependency(%q<rake>.freeze, [">= 0"])
    s.add_dependency(%q<rspec>.freeze, ["~> 3.4"])
    s.add_dependency(%q<pry>.freeze, ["~> 0.12"])
    s.add_dependency(%q<coveralls>.freeze, ["~> 0.8"])
    s.add_dependency(%q<coco>.freeze, ["~> 0.15"])
    s.add_dependency(%q<wwtd>.freeze, ["~> 1.3"])
  end
end
