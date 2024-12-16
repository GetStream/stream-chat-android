# frozen_string_literal: true

module RuboCop
  # Similar to `Forwardable#def_delegators`, but simpler & faster
  module SimpleForwardable
    def def_delegators(accessor, *methods)
      methods.each do |method|
        class_eval(<<~RUBY, __FILE__, __LINE__ + 1)
          def #{method}(...)           # def example(...)
            #{accessor}.#{method}(...) #   foo.example(...)
          end                          # end
        RUBY
      end
    end
  end
end
