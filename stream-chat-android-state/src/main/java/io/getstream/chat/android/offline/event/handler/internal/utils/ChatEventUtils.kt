package io.getstream.chat.android.offline.event.handler.internal.utils

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent

internal val ChatEvent.realType get() = when (this) {
    is ConnectedEvent -> "connection.connected"
    else -> type
}