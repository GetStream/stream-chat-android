require 'forwardable'

module Faye
  class WebSocket

    class Client
      extend Forwardable
      include API

      DEFAULT_PORTS    = { 'http' => 80, 'https' => 443, 'ws' => 80, 'wss' => 443 }
      SECURE_PROTOCOLS = ['https', 'wss']

      def_delegators :@driver, :headers, :status

      def initialize(url, protocols = nil, options = {})
        @url = url
        super(options) { ::WebSocket::Driver.client(self, :max_length => options[:max_length], :protocols => protocols) }

        proxy       = options.fetch(:proxy, {})
        @endpoint   = URI.parse(proxy[:origin] || @url)
        port        = @endpoint.port || DEFAULT_PORTS[@endpoint.scheme]
        @origin_tls = options.fetch(:tls, {})
        @socket_tls = proxy[:origin] ? proxy.fetch(:tls, {}) : @origin_tls

        configure_proxy(proxy)

        EventMachine.connect(@endpoint.host, port, Connection) do |conn|
          conn.parent = self
        end
      rescue => error
        on_network_error(error)
      end

    private

      def configure_proxy(proxy)
        return unless proxy[:origin]

        @proxy = @driver.proxy(proxy[:origin])
        @proxy.on(:error) { |error| @driver.emit(:error, error) }

        if headers = proxy[:headers]
          headers.each { |name, value| @proxy.set_header(name, value) }
        end

        @proxy.on(:connect) do
          @proxy = nil
          start_tls(URI.parse(@url), @origin_tls)
          @driver.start
        end
      end

      def start_tls(uri, options)
        return unless SECURE_PROTOCOLS.include?(uri.scheme)

        tls_options = { :sni_hostname => uri.host, :verify_peer => true }.merge(options)
        @ssl_verifier = SslVerifier.new(uri.host, tls_options)
        @stream.start_tls(tls_options)
      end

      def on_connect(stream)
        @stream = stream
        start_tls(@endpoint, @socket_tls)

        worker = @proxy || @driver
        worker.start
      end

      def on_network_error(error)
        emit_error("Network error: #{ @url }: #{ error.message }")
        finalize_close
      end

      def ssl_verify_peer(cert)
        @ssl_verifier.ssl_verify_peer(cert)
      rescue => error
        on_network_error(error)
      end

      def ssl_handshake_completed
        @ssl_verifier.ssl_handshake_completed
      rescue => error
        on_network_error(error)
      end

      module Connection
        attr_accessor :parent

        def connection_completed
          parent.__send__(:on_connect, self)
        end

        def ssl_verify_peer(cert)
          parent.__send__(:ssl_verify_peer, cert)
        end

        def ssl_handshake_completed
          parent.__send__(:ssl_handshake_completed)
        end

        def receive_data(data)
          parent.__send__(:parse, data)
        end

        def unbind(error = nil)
          parent.__send__(:emit_error, error) if error
          parent.__send__(:finalize_close)
        end

        def write(data)
          send_data(data) rescue nil
        end
      end
    end

  end
end
