package io.getstream.chat.ui.sample.util.extensions

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

internal fun User.isCurrentUser(): Boolean {
    return if (ChatDomain.isInitialized) {
        id == ChatDomain.instance().user.value?.id
    } else {
        false
    }
}