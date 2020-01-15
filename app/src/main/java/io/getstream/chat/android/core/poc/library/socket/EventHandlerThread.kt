package io.getstream.chat.android.core.poc.library.socket

class EventHandlerThread(webSocketService: WebSocketService) : Thread() {

    val handler: EventHandler = EventHandler(webSocketService)
}
