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

    return (lastActive ?: updatedAt ?: createdAt)?.let {
        if (it.isInLastMinute()) {
            context.getString(R.string.stream_ui_message_list_header_last_seen_just_now)
        } else {
            context.getString(
                R.string.stream_ui_message_list_header_last_seen_template,
                DateUtils.getRelativeTimeSpanString(it.time).toString()
            )
        }
    } ?: String.EMPTY
}
