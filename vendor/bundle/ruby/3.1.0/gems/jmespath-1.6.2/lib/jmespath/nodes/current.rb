# frozen_string_literal: true
module JMESPath
  # @api private
  module Nodes
    class Current < Node
      def visit(value)
        value
      end
    end
  end
end
