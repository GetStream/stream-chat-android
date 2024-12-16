# frozen_string_literal: true
module JMESPath
  # @api private
  module Nodes
    class Expression < Node
      attr_reader :expression

      def initialize(expression)
        @expression = expression
      end

      def visit(_value)
        self
      end

      def eval(value)
        @expression.visit(value)
      end

      def optimize
        self.class.new(@expression.optimize)
      end
    end
  end
end
