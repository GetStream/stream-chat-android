module RuboCop
  module RequireTools
    # Contains current state of an inspected file
    class State
      attr_accessor :defined_constants
      attr_accessor :const_stack

      def initialize
        self.defined_constants = []
        self.const_stack = []
      end

      def require(file: nil)
        Kernel.require(file)
      rescue NameError, LoadError => ex
        puts "Note: Could not load #{file}:"
        puts ex.message
        puts 'Check your dependencies, they could be circular'
      end

      def require_relative(relative_path: nil)
        Kernel.require_relative(relative_path)
      rescue NameError, LoadError => ex
        puts "Note: Could not load relative file #{relative_path}:"
        puts ex.message
        puts 'Check your dependencies, they could be circular'
      end

      def access_const(const_name: nil, local_only: false)
        name = const_name.to_s.sub(/^:*/, '').sub(/:*$/, '') # Strip leading/trailing ::

        # If const_stack is ["A", "B", "C"] all of A, A::B, A::B::C are valid lookup combinations
        prefixes = self.const_stack.reduce([]) { |a, c| a << [a.last, c].compact.join('::') }

        # I use const_get here because in testing const_get and const_defined? have yielded different results
        unless local_only
          result = Object.const_get(name) rescue nil                                                   # Defined elsewhere, top-level
          result ||= self.defined_constants.find { |c| Object.const_get("#{c}::#{name}") rescue nil }  # Defined elsewhere, nested
        end

        result ||= self.defined_constants.find { |c| name == c }                                       # Defined in this file, other module/class
        prefixes.each do |prefix|
          result ||= self.defined_constants.find { |c| [name, "#{prefix}::#{name}"].include? c }       # Defined in this file, other module/class
          result ||= prefix == name                                                                    # Defined in this file, in current module/class
        end

        return result
      end

      def define_const(const_name: nil, is_part_of_stack: true)
        new = []
        self.defined_constants.each do |c|
          found = Object.const_get("#{c}::#{const_name}") rescue nil
          new << found.to_s if found
        end
        self.defined_constants.push(*new)
        self.const_stack.push(const_name) if is_part_of_stack
        self.defined_constants.push(const_name.to_s, self.const_stack.join('::'))
        self.defined_constants.uniq!
      end

      def undefine_const(const_name: nil) # rubocop:disable Lint/UnusedMethodArgument
        self.const_stack.pop
      end

      def const_assigned(const_name: nil)
        full_name = (self.const_stack + [const_name]).join('::')
        self.defined_constants << full_name
        self.defined_constants.uniq!
      end
    end
  end
end
