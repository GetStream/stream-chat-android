package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.Event


data class EventResponse(val event: Event) {
    var duration: String = ""
}
