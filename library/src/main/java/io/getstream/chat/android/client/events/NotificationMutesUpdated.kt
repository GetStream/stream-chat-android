package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.models.User

class NotificationMutesUpdated: ChatEvent() {
    lateinit var me: User
}