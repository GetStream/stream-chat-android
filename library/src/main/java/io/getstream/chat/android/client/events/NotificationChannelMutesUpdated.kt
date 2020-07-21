package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.models.User

class NotificationChannelMutesUpdated : ChatEvent() {
    lateinit var me: User
}
