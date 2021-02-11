package io.getstream.chat.android.ui.common.extensions

import android.content.Context
import android.text.format.DateUtils
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY

public fun User.getLastSeenText(context: Context): String {
    if (online) {
        return context.getString(R.string.stream_ui_message_list_header_online)
    }

    (lastActive ?: createdAt)?.let { date ->
        return context.getString(
            R.string.stream_ui_message_list_header_last_seen,
            DateUtils.getRelativeTimeSpanString(date.time).toString()
        )
    }

    return String.EMPTY
}
