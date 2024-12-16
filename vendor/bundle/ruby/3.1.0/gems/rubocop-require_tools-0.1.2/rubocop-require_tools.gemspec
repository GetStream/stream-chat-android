lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'rubocop/require_tools/version'

Gem::Specification.new do |spec|
  spec.name          = 'rubocop-require_tools'
  spec.version       = RuboCop::RequireTools::VERSION
  spec.authors       = ['Manu Wallner']
  spec.email         = ['manu@supermil.ch']

  spec.summary       = 'Checks require statements in your code'
  spec.description   = <<-DESC.gsub(/^\s\s/, '')
  Available Cops in this extension:

  - MissingRequireStatement:
    Missing require statements can cause implicit dependencies in your projects that are error prone and hard to maintain.
  DESC

  spec.homepage      = 'https://github.com/milch/rubocop-require_tools'
  spec.license       = 'MIT'

  spec.files         = `git ls-files -z`.split("\x0").reject do |f|
    f.match(%r{^(test|spec|features)/})
  end
  spec.bindir        = 'exe'
  spec.executables   = spec.files.grep(%r{^exe/}) { |f| File.basename(f) }
  spec.require_paths = ['lib']

  spec.add_runtime_dependency 'rubocop', '>= 0.49.1'

  spec.add_development_dependency 'pry'

  spec.add_development_dependency 'bundler', '~> 1.16'
  spec.add_development_dependency 'rake', '~> 10.0'
  spec.add_development_dependency 'rspec', '~> 3.0'
end
