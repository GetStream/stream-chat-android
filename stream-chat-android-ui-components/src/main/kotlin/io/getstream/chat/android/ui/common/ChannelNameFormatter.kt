package io.getstream.chat.android.ui.common

import android.content.Context
import androidx.annotation.StringRes
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.getDisplayName

/**
 *  An interface that generates a name for the given channel.
 */
public fun interface ChannelNameFormatter {

    /**
     * Generates a name for the given channel.
     *
     * @param channel The channel data used to generate the name.
     * @param currentUser The currently logged in user.
     * @return The display name for the given channel.
     */
    public fun formatChannelName(channel: Channel, currentUser: User?): String

    public companion object {
        /**
         * Builds the default channel name formatter.
         *
         * @param context The context used to load string resources.
         * @param fallback The resource identifier of a fallback string.
         *
         * @see [DefaultChannelNameFormatter]
         */
        public fun defaultFormatter(
            context: Context,
            @StringRes fallback: Int = R.string.stream_ui_channel_list_untitled_channel,
        ): ChannelNameFormatter {
            return DefaultChannelNameFormatter(context, fallback)
        }
    }
}

/**
 * A simple implementation of [ChannelNameFormatter] that generates the name for a channel
 * based on the following rules:
 *
 * - If the channel has a name, then its name is returned
 * - If the channel is distinct, then a comma-separated list of member names is returned
 * - Otherwise, the placeholder text defined in [R.string.stream_ui_channel_list_untitled_channel] is returned
 *
 * @param context The context used to load string resources.
 * @param fallback The resource identifier of a fallback string.
 */
private class DefaultChannelNameFormatter(
    private val context: Context,
    @StringRes private val fallback: Int,
) : ChannelNameFormatter {

    /**
     * Generates a name for the given channel.
     *
     * @param channel The channel whose data is used to generate the name.
     * @param currentUser The currently logged in user.
     * @return The display name for the given channel.
     */
    override fun formatChannelName(channel: Channel, currentUser: User?): String {
        return channel.getDisplayName(context, currentUser, fallback)
    }
}
