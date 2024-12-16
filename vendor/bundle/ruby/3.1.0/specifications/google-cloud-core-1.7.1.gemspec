# -*- encoding: utf-8 -*-
# stub: google-cloud-core 1.7.1 ruby lib

Gem::Specification.new do |s|
  s.name = "google-cloud-core".freeze
  s.version = "1.7.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Mike Moore".freeze, "Chris Smith".freeze]
  s.date = "2024-08-05"
  s.description = "google-cloud-core is the internal shared library for google-cloud-ruby.".freeze
  s.email = ["mike@blowmage.com".freeze, "quartzmo@gmail.com".freeze]
  s.homepage = "https://github.com/googleapis/google-cloud-ruby/tree/master/google-cloud-core".freeze
  s.licenses = ["Apache-2.0".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.7".freeze)
  s.rubygems_version = "3.3.27".freeze
  s.summary = "Internal shared library for google-cloud-ruby".freeze

  s.installed_by_version = "3.3.27" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_runtime_dependency(%q<google-cloud-env>.freeze, [">= 1.0", "< 3.a"])
    s.add_runtime_dependency(%q<google-cloud-errors>.freeze, ["~> 1.0"])
  else
    s.add_dependency(%q<google-cloud-env>.freeze, [">= 1.0", "< 3.a"])
    s.add_dependency(%q<google-cloud-errors>.freeze, ["~> 1.0"])
  end
end
