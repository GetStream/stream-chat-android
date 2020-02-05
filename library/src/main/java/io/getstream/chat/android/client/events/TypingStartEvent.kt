package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.models.User

class TypingStartEvent : RemoteEvent() {
    lateinit var cid: String
    lateinit var user: User
}