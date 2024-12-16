require File.expand_path('../api/event_target', __FILE__)
require File.expand_path('../api/event', __FILE__)

module Faye
  class WebSocket

    module API
      CONNECTING = 0
      OPEN       = 1
      CLOSING    = 2
      CLOSED     = 3

      CLOSE_TIMEOUT = 30

      include EventTarget

      extend Forwardable
      def_delegators :@driver, :version

      attr_reader :url, :ready_state, :buffered_amount

      def initialize(options = {})
        @ready_state = CONNECTING
        super()
        ::WebSocket::Driver.validate_options(options, [:headers, :extensions, :max_length, :ping, :proxy, :tls])

        @driver = yield

        if headers = options[:headers]
          headers.each { |name, value| @driver.set_header(name, value) }
        end

        [*options[:extensions]].each do |extension|
          @driver.add_extension(extension)
        end

        @ping            = options[:ping]
        @ping_id         = 0
        @buffered_amount = 0

        @close_params = @close_timer = @ping_timer = @proxy = @stream = nil
        @onopen = @onmessage = @onclose = @onerror = nil

        @driver.on(:open)    { |e| open }
        @driver.on(:message) { |e| receive_message(e.data) }
        @driver.on(:close)   { |e| begin_close(e.reason, e.code, :wait_for_write => true) }

        @driver.on(:error) do |error|
          emit_error(error.message)
        end

        if @ping
          @ping_timer = EventMachine.add_periodic_timer(@ping) do
            @ping_id += 1
            ping(@ping_id.to_s)
          end
        end
      end

      def write(data)
        @stream.write(data)
      end

      def send(message)
        return false if @ready_state > OPEN
        case message
          when Numeric then @driver.text(message.to_s)
          when String  then @driver.text(message)
          when Array   then @driver.binary(message)
          else false
        end
      end

      def ping(message = '', &callback)
        return false if @ready_state > OPEN
        @driver.ping(message, &callback)
      end

      def close(code = nil, reason = nil)
        code   ||= 1000
        reason ||= ''

        unless code == 1000 or (code >= 3000 and code <= 4999)
          raise ArgumentError, "Failed to execute 'close' on WebSocket: " +
                               "The code must be either 1000, or between 3000 and 4999. " +
                               "#{ code } is neither."
        end

        if @ready_state < CLOSING
          @close_timer = EventMachine.add_timer(CLOSE_TIMEOUT) { begin_close('', 1006) }
        end

        @ready_state = CLOSING unless @ready_state == CLOSED

        @driver.close(reason, code)
      end

      def protocol
        @driver.protocol || ''
      end

    private

      def open
        return unless @ready_state == CONNECTING
        @ready_state = OPEN
        event = Event.create('open')
        event.init_event('open', false, false)
        dispatch_event(event)
      end

      def receive_message(data)
        return unless @ready_state == OPEN
        event = Event.create('message', :data => data)
        event.init_event('message', false, false)
        dispatch_event(event)
      end

      def emit_error(message)
        return if @ready_state >= CLOSING

        event = Event.create('error', :message => message)
        event.init_event('error', false, false)
        dispatch_event(event)
      end

      def begin_close(reason, code, options = {})
        return if @ready_state == CLOSED
        @ready_state = CLOSING
        @close_params = [reason, code]

        if @stream
          if options[:wait_for_write]
            @stream.close_connection_after_writing
          else
            @stream.close_connection
          end
        else
          finalize_close
        end
      end

      def finalize_close
        return if @ready_state == CLOSED
        @ready_state = CLOSED

        EventMachine.cancel_timer(@close_timer) if @close_timer
        EventMachine.cancel_timer(@ping_timer) if @ping_timer

        reason = @close_params ? @close_params[0] : ''
        code   = @close_params ? @close_params[1] : 1006

        event = Event.create('close', :code => code, :reason => reason)
        event.init_event('close', false, false)
        dispatch_event(event)
      end

      def parse(data)
        worker = @proxy || @driver
        worker.parse(data)
      end
    end

  end
end
