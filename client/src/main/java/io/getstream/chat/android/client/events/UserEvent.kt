package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.models.User

interface UserEvent {
    val user: User
}
