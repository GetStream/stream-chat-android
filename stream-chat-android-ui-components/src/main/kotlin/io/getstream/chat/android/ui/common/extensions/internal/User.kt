package io.getstream.chat.android.ui.common.extensions.internal

import android.content.Context
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R

internal fun User.isCurrentUser(): Boolean {
    return if (ChatDomain.isInitialized) {
        id == ChatDomain.instance().user.value?.id
    } else {
        false
    }
}

internal fun User.asMention(context: Context): String =
    context.getString(R.string.stream_ui_mention, name)
