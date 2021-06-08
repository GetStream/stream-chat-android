package io.getstream.chat.android.ui.common.extensions

import android.content.Context
import androidx.annotation.StringRes
import com.getstream.sdk.chat.utils.extensions.getUsers
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R

/**
 * Returns the channel name if exists, or the list of member names if the channel is distinct.
 *
 * @param devValue The resource identifier of a fallback string if the [Channel] object lacks
 * information to construct a valid display name string.
 *
 * @return The display name of the channel
 */
@JvmOverloads
public fun Channel.getDisplayName(
    context: Context,
    @StringRes devValue: Int = R.string.stream_ui_channel_list_untitled_channel,
): String {
    return name.takeIf { it.isNotEmpty() }
        ?: getUsers()
            .joinToString { it.name }
            .takeIf { it.isNotEmpty() }
        ?: context.getString(devValue)
}
