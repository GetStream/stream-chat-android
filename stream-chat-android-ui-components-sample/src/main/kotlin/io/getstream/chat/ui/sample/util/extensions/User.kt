package io.getstream.chat.ui.sample.util.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User

internal fun User.isCurrentUser(): Boolean {
    return if (ChatClient.isInitialized) {
        id == ChatClient.instance().getCurrentUser()?.id
    } else {
        false
    }
}