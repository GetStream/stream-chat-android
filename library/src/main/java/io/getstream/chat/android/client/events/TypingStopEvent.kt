package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.User

class TypingStopEvent : RemoteEvent() {
    lateinit var cid: String
    lateinit var user: User
}