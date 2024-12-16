require File.expand_path('../websocket', __FILE__) unless defined?(Faye::WebSocket)

module Faye
  class EventSource

    include WebSocket::API::EventTarget
    attr_reader :env, :url, :ready_state

    DEFAULT_RETRY = 5

    def self.eventsource?(env)
      return false unless env['REQUEST_METHOD'] == 'GET'
      accept = (env['HTTP_ACCEPT'] || '').split(/\s*,\s*/)
      accept.include?('text/event-stream')
    end

    def self.determine_url(env)
      WebSocket.determine_url(env, ['https', 'http'])
    end

    def initialize(env, options = {})
      WebSocket.ensure_reactor_running
      super()

      @env    = env
      @ping   = options[:ping]
      @retry  = (options[:retry] || DEFAULT_RETRY).to_f
      @url    = EventSource.determine_url(env)
      @stream = Stream.new(self)

      @ready_state = WebSocket::API::CONNECTING

      headers = ::WebSocket::Driver::Headers.new
      if options[:headers]
        options[:headers].each { |k,v| headers[k] = v }
      end

      if callback = @env['async.callback']
        callback.call([101, {}, @stream])
      end

      @stream.write("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/event-stream\r\n" +
                    "Cache-Control: no-cache, no-store\r\n" +
                    "Connection: close\r\n" +
                    headers.to_s +
                    "\r\n" +
                    "retry: #{ (@retry * 1000).floor }\r\n\r\n")

      EventMachine.next_tick { open }

      if @ping
        @ping_timer = EventMachine.add_periodic_timer(@ping) { ping }
      end
    end

    def last_event_id
      @env['HTTP_LAST_EVENT_ID'] || ''
    end

    def rack_response
      [ -1, {}, [] ]
    end

  private

    def open
      return unless @ready_state == WebSocket::API::CONNECTING

      @ready_state = WebSocket::API::OPEN

      event = WebSocket::API::Event.create('open')
      event.init_event('open', false, false)
      dispatch_event(event)
    end

  public

    def send(message, options = {})
      return false if @ready_state > WebSocket::API::OPEN

      message = ::WebSocket::Driver.encode(message.to_s).
                gsub(/(\r\n|\r|\n)/, '\1data: ')

      frame  = ""
      frame << "event: #{ options[:event] }\r\n" if options[:event]
      frame << "id: #{ options[:id] }\r\n" if options[:id]
      frame << "data: #{ message }\r\n\r\n"

      @stream.write(frame)
      true
    end

    def ping(message = nil)
      return false if @ready_state > WebSocket::API::OPEN
      @stream.write(":\r\n\r\n")
      true
    end

    def close
      return if [WebSocket::API::CLOSING, WebSocket::API::CLOSED].include?(@ready_state)

      @ready_state = WebSocket::API::CLOSED
      EventMachine.cancel_timer(@ping_timer)
      @stream.close_connection_after_writing

      event = WebSocket::API::Event.create('close')
      event.init_event('close', false, false)
      dispatch_event(event)
    end

    class Stream < RackStream
      def fail
        @socket_object.close
      end
    end

  end
end
