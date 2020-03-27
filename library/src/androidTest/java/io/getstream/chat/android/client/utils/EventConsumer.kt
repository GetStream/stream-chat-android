package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.events.ChatEvent

class EventConsumer(val type: Class<out ChatEvent>) {

    var event: ChatEvent? = null

    fun onEvent(event: ChatEvent) {
        if (type.isInstance(event)) {
            this.event = event
        }
    }

    fun isReceived(): Boolean {
        return event != null
    }
}