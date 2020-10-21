package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.events.ChatEvent

class EventsConsumer(val expected: List<Class<out ChatEvent>>) {

    var received = mutableListOf<ChatEvent>()

    fun onEvent(event: ChatEvent) {
        received.add(event)
    }

    fun isReceived(): Boolean {

        expected.forEach { expectedType ->
            received.forEach { event ->
                if (expectedType.isInstance(event)) {
                    return true
                }
            }
        }

        return false
    }

    fun isReceivedExactly(check: List<Class<out ChatEvent>>): Boolean {
        return check == expected
    }
}
