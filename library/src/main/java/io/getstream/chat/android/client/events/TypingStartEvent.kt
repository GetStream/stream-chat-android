package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.models.User

class TypingStartEvent : ChatEvent() {
    lateinit var cid: String
}