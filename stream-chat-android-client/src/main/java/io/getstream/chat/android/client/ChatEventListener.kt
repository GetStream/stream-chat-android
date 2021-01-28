package io.getstream.chat.android.client

import io.getstream.chat.android.client.events.ChatEvent

public fun interface ChatEventListener<EventT : ChatEvent> {
    public fun onEvent(event: EventT)
}
