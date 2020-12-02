package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.text.format.DateUtils
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R

internal fun List<User>.withoutCurrentUser() = this.filter { !it.isCurrentUser() }

internal fun User.isCurrentUser(): Boolean = id == ChatDomain.instance().currentUser.id

internal fun User.getLastSeenText(context: Context): String {
    return if (online) {
        context.getString(R.string.stream_message_list_header_online)
    } else {
        val lastActive = lastActive ?: return String.EMPTY
        context.getString(
            R.string.stream_message_list_header_last_seen,
            DateUtils.getRelativeTimeSpanString(lastActive.time).toString()
        )
    }
}
