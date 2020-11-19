package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name

internal fun Message.getDisplayName() = when {
    user.isCurrentUser() -> "You"
    else -> user.name
}
