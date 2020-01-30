package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.events.ChatEvent


data class EventResponse(val event: ChatEvent) {
    var duration: String = ""
}
