package io.getstream.chat.android.core.poc.library.socket

class EventHandlerThread(webSocketService: WebSocketService) : Thread() {

    var mHandler: EventHandler

    init {
        mHandler = EventHandler(webSocketService)
    }
}
