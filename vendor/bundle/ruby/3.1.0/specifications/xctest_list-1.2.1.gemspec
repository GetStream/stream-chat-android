# -*- encoding: utf-8 -*-
# stub: xctest_list 1.2.1 ruby lib

Gem::Specification.new do |s|
  s.name = "xctest_list".freeze
  s.version = "1.2.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Lyndsey Ferguson".freeze]
  s.date = "2018-08-15"
  s.description = "A gem to retrieve the tests in an iOS xctest bundle".freeze
  s.email = "ldf.public+github@outlook.com".freeze
  s.homepage = "https://github.com/lyndsey-ferguson/xctest_list".freeze
  s.licenses = ["MIT".freeze]
  s.rubygems_version = "3.3.27".freeze
  s.summary = "List the tests in the given xctest bundle".freeze

  s.installed_by_version = "3.3.27" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_development_dependency(%q<colorize>.freeze, [">= 0"])
  else
    s.add_dependency(%q<colorize>.freeze, [">= 0"])
  end
end
