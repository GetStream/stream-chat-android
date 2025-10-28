/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.common.utils

import android.content.Context
import androidx.annotation.StringRes
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.uiutils.extension.getDisplayName

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
         * @param maxMembers The maximum number of members used to generate a name for a distinct channel.
         *
         * @see [DefaultChannelNameFormatter]
         */
        @JvmOverloads
        public fun defaultFormatter(
            context: Context,
            @StringRes fallback: Int = R.string.stream_ui_channel_list_untitled_channel,
            maxMembers: Int = 2,
        ): ChannelNameFormatter = DefaultChannelNameFormatter(context, fallback, maxMembers)
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
 * @param maxMembers The maximum number of members used to generate a name for a distinct channel.
 */
private class DefaultChannelNameFormatter(
    private val context: Context,
    @StringRes private val fallback: Int,
    private val maxMembers: Int,
) : ChannelNameFormatter {

    /**
     * Generates a name for the given channel.
     *
     * @param channel The channel whose data is used to generate the name.
     * @param currentUser The currently logged in user.
     * @return The display name for the given channel.
     */
    override fun formatChannelName(channel: Channel, currentUser: User?): String = channel.getDisplayName(context, currentUser, fallback, maxMembers)
}
