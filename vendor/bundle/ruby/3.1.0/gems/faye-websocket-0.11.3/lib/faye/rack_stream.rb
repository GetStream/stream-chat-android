module Faye
  class RackStream

    include EventMachine::Deferrable

    module Reader
      attr_accessor :stream

      def receive_data(data)
        stream.receive(data)
      end

      def unbind
        stream.fail
      end
    end

    def initialize(socket)
      @socket_object = socket
      @connection    = socket.env['em.connection']
      @stream_send   = socket.env['stream.send']

      @rack_hijack_io = @rack_hijack_io_reader = nil

      hijack_rack_socket

      @connection.socket_stream = self if @connection.respond_to?(:socket_stream)
    end

    def hijack_rack_socket
      return unless @socket_object.env['rack.hijack']

      @socket_object.env['rack.hijack'].call
      @rack_hijack_io = @socket_object.env['rack.hijack_io']
      queue = Queue.new

      EventMachine.schedule do
        begin
          EventMachine.attach(@rack_hijack_io, Reader) do |reader|
            reader.stream = self
            if @rack_hijack_io
              @rack_hijack_io_reader = reader
            else
              reader.close_connection_after_writing
            end
          end
        ensure
          queue.push(nil)
        end
      end

      queue.pop if EventMachine.reactor_running?
    end

    def clean_rack_hijack
      return unless @rack_hijack_io
      @rack_hijack_io_reader.close_connection_after_writing
      @rack_hijack_io = @rack_hijack_io_reader = nil
    end

    def close_connection
      clean_rack_hijack
      @connection.close_connection if @connection
    end

    def close_connection_after_writing
      clean_rack_hijack
      @connection.close_connection_after_writing if @connection
    end

    def each(&callback)
      @stream_send ||= callback
    end

    def fail
    end

    def receive(data)
    end

    def write(data)
      return @rack_hijack_io_reader.send_data(data) if @rack_hijack_io_reader
      return @rack_hijack_io.write(data) if @rack_hijack_io
      return @stream_send.call(data) if @stream_send
    rescue => e
      fail if EOFError === e
    end

  end
end
