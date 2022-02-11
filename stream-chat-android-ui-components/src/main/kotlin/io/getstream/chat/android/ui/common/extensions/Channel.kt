package io.getstream.chat.android.ui.common.extensions

import android.content.Context
import androidx.annotation.StringRes
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.isCurrentUser

/**
 * Returns the channel name if exists, or the list of member names if the channel is distinct.
 *
 * @param context The context to load string resources.
 * @param currentUser The currently logged-in user.
 * @param fallback The resource identifier of a fallback string if the [Channel] object lacks
 * information to construct a valid display name string.
 *
 * @return The display name of the channel.
 */
@InternalStreamChatApi
@JvmOverloads
public fun Channel.getDisplayName(
    context: Context,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    @StringRes fallback: Int = R.string.stream_ui_channel_list_untitled_channel,
): String {
    return name.takeIf { it.isNotEmpty() }
        ?: getUsersExcludingCurrent(currentUser)
            .joinToString { it.name }
            .takeIf { it.isNotEmpty() }
        ?: context.getString(fallback)
}

/**
 * Returns channel's last regular or system message if exists.
 * Deleted and silent messages, as well as messages from shadow-banned users, are not taken into account.
 *
 * @return Last message from the channel or null if it doesn't exist.
 */
public fun Channel.getLastMessage(): Message? =
    messages.asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filter { it.deletedAt == null }
        .filter { !it.silent }
        .filter { it.user.isCurrentUser() || !it.shadowed }
        .filter { it.isRegular() || it.isSystem() }
        .maxByOrNull { it.getCreatedAtOrThrow() }
