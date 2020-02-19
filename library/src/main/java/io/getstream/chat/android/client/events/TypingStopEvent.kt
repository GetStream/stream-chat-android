package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.models.User

class TypingStopEvent : ChatEvent() {
    lateinit var cid: String
}