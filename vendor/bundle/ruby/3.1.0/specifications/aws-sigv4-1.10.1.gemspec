# -*- encoding: utf-8 -*-
# stub: aws-sigv4 1.10.1 ruby lib

Gem::Specification.new do |s|
  s.name = "aws-sigv4".freeze
  s.version = "1.10.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.metadata = { "changelog_uri" => "https://github.com/aws/aws-sdk-ruby/tree/version-3/gems/aws-sigv4/CHANGELOG.md", "source_code_uri" => "https://github.com/aws/aws-sdk-ruby/tree/version-3/gems/aws-sigv4" } if s.respond_to? :metadata=
  s.require_paths = ["lib".freeze]
  s.authors = ["Amazon Web Services".freeze]
  s.date = "2024-10-21"
  s.description = "Amazon Web Services Signature Version 4 signing library. Generates sigv4 signature for HTTP requests.".freeze
  s.homepage = "https://github.com/aws/aws-sdk-ruby".freeze
  s.licenses = ["Apache-2.0".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.5".freeze)
  s.rubygems_version = "3.3.27".freeze
  s.summary = "AWS Signature Version 4 library.".freeze

  s.installed_by_version = "3.3.27" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4
  end

  if s.respond_to? :add_runtime_dependency then
    s.add_runtime_dependency(%q<aws-eventstream>.freeze, ["~> 1", ">= 1.0.2"])
  else
    s.add_dependency(%q<aws-eventstream>.freeze, ["~> 1", ">= 1.0.2"])
  end
end
