package io.getstream.chat.android.core.poc.library.rest

import io.getstream.chat.android.core.poc.library.Event


class EventResponse {
    lateinit var event: Event
    var duration: String = ""
}
