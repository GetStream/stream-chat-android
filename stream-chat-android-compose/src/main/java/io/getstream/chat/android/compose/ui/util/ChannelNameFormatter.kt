package io.getstream.chat.android.compose.ui.util

import android.content.Context
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.compose.R

/**
 *  An interface that allows to generate a name for the given channel.
 */
public fun interface ChannelNameFormatter {

    /**
     * Generates a name for the given channel.
     *
     * @param channel The channel whose data is used to generate the name.
     * @return The display name for the given channel.
     */
    public fun formatChannelName(channel: Channel): String

    public companion object {
        /**
         * Build the default channel name formatter.
         *
         * @param context The context to load string resources.
         * @param maxMembers The maximum number of members used to generate a name for a distinct channel.
         *
         * @see [DefaultChannelNameFormatter]
         */
        public fun defaultFormatter(
            context: Context,
            maxMembers: Int = 5,
        ): ChannelNameFormatter {
            return DefaultChannelNameFormatter(context, maxMembers)
        }
    }
}

/**
 * A simple implementation of [ChannelNameFormatter] that allows to generate a name for a channel
 * based on the following rules:
 *
 * - If the channel has a name, then its name is returned
 * - If the channel is distinct, then a comma-separated list of member names is returned
 * - Otherwise, the placeholder text defined in [R.string.stream_compose_untitled_channel] is returned
 *
 * @param context The context to load string resources.
 * @param maxMembers The maximum number of members used to generate a name for a distinct channel.
 */
private class DefaultChannelNameFormatter(
    private val context: Context,
    private val maxMembers: Int,
) : ChannelNameFormatter {

    /**
     * Generates a name for the given channel.
     *
     * @param channel The channel whose data is used to generate the name.
     * @return The display name for the given channel.
     */
    override fun formatChannelName(channel: Channel): String {
        return channel.name.takeIf { it.isNotEmpty() }
            ?: channel.getUsersExcludingCurrent()
                .joinToString(limit = maxMembers) { it.name }
                .takeIf { it.isNotEmpty() }
            ?: context.getString(R.string.stream_compose_untitled_channel)
    }
}
