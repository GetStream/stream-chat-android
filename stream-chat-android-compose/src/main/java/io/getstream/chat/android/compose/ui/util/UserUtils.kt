package io.getstream.chat.android.compose.ui.util

import android.content.Context
import android.text.format.DateUtils
import com.getstream.sdk.chat.utils.extensions.isInLastMinute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R

/**
 * Returns a string describing the elapsed time since the user was online (was watching the channel).
 *
 * Depending on the elapsed time, the string can have one of the following formats:
 * - Online
 * - Last seen just now
 * - Last seen 13 hours ago
 *
 * @return A string that represents the elapsed time since the user was online.
 */
public fun User.getLastSeenText(context: Context): String {
    if (online) {
        return context.getString(R.string.stream_compose_user_status_online)
    }

    return (lastActive ?: updatedAt ?: createdAt)?.let {
        if (it.isInLastMinute()) {
            context.getString(R.string.stream_compose_user_status_last_seen_just_now)
        } else {
            context.getString(
                R.string.stream_compose_user_status_last_seen,
                DateUtils.getRelativeTimeSpanString(it.time).toString()
            )
        }
    } ?: ""
}
