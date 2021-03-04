package io.getstream.chat.android.ui.common.extensions

import android.content.Context
import android.text.format.DateUtils
import com.getstream.sdk.chat.utils.extensions.isInLastMinute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY

public fun User.getLastSeenText(context: Context): String {
    if (online) {
        return context.getString(R.string.stream_ui_message_list_header_online)
    }

    return (updatedAt ?: lastActive ?: createdAt)?.let {
        val date = when {
            it.isInLastMinute() -> context.getString(R.string.stream_ui_message_list_header_just_now)
            else -> DateUtils.getRelativeTimeSpanString(it.time).toString()
        }
        context.getString(R.string.stream_ui_message_list_header_last_seen, date)
    } ?: String.EMPTY
}
