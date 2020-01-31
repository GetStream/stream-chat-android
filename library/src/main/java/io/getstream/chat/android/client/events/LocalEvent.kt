package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.EventType

open class LocalEvent(type: EventType) : ChatEvent() {
    init {
        this.type = type.label
    }
}