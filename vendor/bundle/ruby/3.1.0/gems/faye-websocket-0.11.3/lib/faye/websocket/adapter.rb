module Faye
  class WebSocket

    module Adapter
      def websocket?
        e = defined?(@env) ? @env : env
        e && WebSocket.websocket?(e)
      end

      def eventsource?
        e = defined?(@env) ? @env : env
        e && EventSource.eventsource?(e)
      end

      def socket_connection?
        websocket? or eventsource?
      end
    end

  end
end
