package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.events.ChatEvent


data class EventResponse(val event: ChatEvent) {
    var duration: String = ""
}
