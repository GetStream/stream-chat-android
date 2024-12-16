class Goliath::Connection
  attr_accessor :socket_stream
  alias :goliath_receive_data :receive_data

  def receive_data(data)
    if @serving == :websocket
      socket_stream.receive(data) if socket_stream
    else
      goliath_receive_data(data)
      socket_stream.receive(@parser.upgrade_data) if socket_stream
      @serving = :websocket if @api.websocket?
    end
  end

  def unbind
    super
  ensure
    socket_stream.fail if socket_stream
  end
end

class Goliath::API
  include Faye::WebSocket::Adapter
  alias :goliath_call :call

  def call(env)
    @env = env
    goliath_call(env)
  end
end

class Goliath::Request
  alias :goliath_process :process

  def process
    env['em.connection'] = conn
    goliath_process
  end
end

class Goliath::Response
  alias :goliath_head :head
  alias :goliath_headers_output :headers_output

  def head
    (status == 101) ? '' : goliath_head
  end

  def headers_output
    (status == 101) ? '' : goliath_headers_output
  end
end
