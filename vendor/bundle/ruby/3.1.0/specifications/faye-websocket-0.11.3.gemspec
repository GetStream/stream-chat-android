# -*- encoding: utf-8 -*-
# stub: faye-websocket 0.11.3 ruby lib

Gem::Specification.new do |s|
  s.name = "faye-websocket".freeze
  s.version = "0.11.3"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["James Coglan".freeze]
  s.date = "2023-07-25"
  s.email = "jcoglan@gmail.com".freeze
  s.extra_rdoc_files = ["README.md".freeze]
  s.files = ["README.md".freeze]
  s.homepage = "https://github.com/faye/faye-websocket-ruby".freeze
  s.licenses = ["Apache-2.0".freeze]
  s.rdoc_options = ["--main".freeze, "README.md".freeze, "--markup".freeze, "markdown".freeze]
  s.rubygems_version = "3.3.27".freeze
  s.summary = "Standards-compliant WebSocket server and client".freeze

  s.installed_by_version = "3.3.27" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_runtime_dependency(%q<eventmachine>.freeze, [">= 0.12.0"])
    s.add_runtime_dependency(%q<websocket-driver>.freeze, [">= 0.5.1"])
    s.add_development_dependency(%q<permessage_deflate>.freeze, [">= 0"])
    s.add_development_dependency(%q<progressbar>.freeze, [">= 0"])
    s.add_development_dependency(%q<puma>.freeze, [">= 2.0.0", "< 6.0"])
    s.add_development_dependency(%q<rack>.freeze, [">= 0"])
    s.add_development_dependency(%q<rspec>.freeze, [">= 0"])
    s.add_development_dependency(%q<rspec-eventmachine>.freeze, [">= 0.2.0"])
    s.add_development_dependency(%q<thin>.freeze, [">= 1.2.0", "> 0"])
    s.add_development_dependency(%q<rainbows>.freeze, ["~> 4.4.0"])
    s.add_development_dependency(%q<goliath>.freeze, ["> 0"])
    s.add_development_dependency(%q<passenger>.freeze, [">= 4.0.0"])
  else
    s.add_dependency(%q<eventmachine>.freeze, [">= 0.12.0"])
    s.add_dependency(%q<websocket-driver>.freeze, [">= 0.5.1"])
    s.add_dependency(%q<permessage_deflate>.freeze, [">= 0"])
    s.add_dependency(%q<progressbar>.freeze, [">= 0"])
    s.add_dependency(%q<puma>.freeze, [">= 2.0.0", "< 6.0"])
    s.add_dependency(%q<rack>.freeze, [">= 0"])
    s.add_dependency(%q<rspec>.freeze, [">= 0"])
    s.add_dependency(%q<rspec-eventmachine>.freeze, [">= 0.2.0"])
    s.add_dependency(%q<thin>.freeze, [">= 1.2.0", "> 0"])
    s.add_dependency(%q<rainbows>.freeze, ["~> 4.4.0"])
    s.add_dependency(%q<goliath>.freeze, ["> 0"])
    s.add_dependency(%q<passenger>.freeze, [">= 4.0.0"])
  end
end
