module Faye::WebSocket::API

  class Event
    attr_reader   :type, :bubbles, :cancelable
    attr_accessor :target, :current_target, :event_phase

    CAPTURING_PHASE = 1
    AT_TARGET       = 2
    BUBBLING_PHASE  = 3

    def initialize(event_type, options)
      @type = event_type
      options.each { |key, value| instance_variable_set("@#{ key }", value) }
    end

    def init_event(event_type, can_bubble, cancelable)
      @type       = event_type
      @bubbles    = can_bubble
      @cancelable = cancelable
    end

    def stop_propagation
    end

    def prevent_default
    end
  end

  class OpenEvent < Event
  end

  class MessageEvent < Event
    attr_reader :data
  end

  class CloseEvent < Event
    attr_reader :code, :reason
  end

  class ErrorEvent < Event
    attr_reader :message
  end

  TYPES = {
    'open'    => OpenEvent,
    'message' => MessageEvent,
    'close'   => CloseEvent,
    'error'   => ErrorEvent
  }

  def Event.create(type, options = {})
    TYPES[type].new(type, options)
  end

end
