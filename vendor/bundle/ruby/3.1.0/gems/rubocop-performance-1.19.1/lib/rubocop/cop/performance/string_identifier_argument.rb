# frozen_string_literal: true

module RuboCop
  module Cop
    module Performance
      # Identifies places where string identifier argument can be replaced
      # by symbol identifier argument.
      # It prevents the redundancy of the internal string-to-symbol conversion.
      #
      # This cop targets methods that take identifier (e.g. method name) argument
      # and the following examples are parts of it.
      #
      # @example
      #
      #   # bad
      #   send('do_something')
      #   attr_accessor 'do_something'
      #   instance_variable_get('@ivar')
      #
      #   # good
      #   send(:do_something)
      #   attr_accessor :do_something
      #   instance_variable_get(:@ivar)
      #
      class StringIdentifierArgument < Base
        extend AutoCorrector

        MSG = 'Use `%<symbol_arg>s` instead of `%<string_arg>s`.'

        COMMAND_METHODS = %i[
          alias_method attr_accessor attr_reader attr_writer autoload autoload? private private_constant
          protected public public_constant module_function
        ].freeze

        # NOTE: `attr` method is not included in this list as it can cause false positives in Nokogiri API.
        # And `attr` may not be used because `Style/Attr` registers an offense.
        # https://github.com/rubocop/rubocop-performance/issues/278
        RESTRICT_ON_SEND = (%i[
          class_variable_defined? const_defined? const_get const_set const_source_location
          define_method instance_method method_defined? private_class_method? private_method_defined?
          protected_method_defined? public_class_method public_instance_method public_method_defined?
          remove_class_variable remove_method undef_method class_variable_get class_variable_set
          deprecate_constant remove_const ruby2_keywords define_singleton_method instance_variable_defined?
          instance_variable_get instance_variable_set method public_method public_send remove_instance_variable
          respond_to? send singleton_method __send__
        ] + COMMAND_METHODS).freeze

        def on_send(node)
          return if COMMAND_METHODS.include?(node.method_name) && node.receiver
          return unless (first_argument = node.first_argument)
          return unless first_argument.str_type?

          first_argument_value = first_argument.value
          return if first_argument_value.include?(' ') || first_argument_value.include?('::')

          replacement = first_argument_value.to_sym.inspect

          message = format(MSG, symbol_arg: replacement, string_arg: first_argument.source)

          add_offense(first_argument, message: message) do |corrector|
            corrector.replace(first_argument, replacement)
          end
        end
      end
    end
  end
end
