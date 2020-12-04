package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

internal fun List<User>.withoutCurrentUser() = this.filter { !it.isCurrentUser() }

internal fun User.isCurrentUser(): Boolean {
    return if (ChatDomain.isInitialized) {
        id == ChatDomain.instance().currentUser.id
    } else {
        false
    }
}
