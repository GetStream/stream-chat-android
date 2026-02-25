/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.channels.actions.internal

import android.content.Context
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import io.getstream.chat.android.ui.common.state.channels.actions.LeaveGroup
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo
import io.getstream.chat.android.ui.feature.channels.actions.ChannelActionsDialogViewStyle

/**
 * An interface that allows the creation of channel action items.
 */
internal interface ChannelOptionItemsFactory {

    /**
     * Creates [ChannelAction]s for the selected channel.
     *
     * @param selectedChannel The currently selected channel.
     * @param ownCapabilities Set of capabilities the user is given for the current channel.
     * @param style The style of the dialog.
     * @return The list of channel actions to display.
     */
    fun createChannelOptionItems(
        selectedChannel: Channel,
        ownCapabilities: Set<String>,
        style: ChannelActionsDialogViewStyle,
    ): List<ChannelAction>

    companion object {
        /**
         * Builds the default channel option items factory.
         *
         * @return The default implementation of [ChannelOptionItemsFactory].
         */
        fun defaultFactory(context: Context): ChannelOptionItemsFactory {
            return DefaultChannelOptionItemsFactory(context)
        }
    }
}

/**
 * The default implementation of [ChannelOptionItemsFactory].
 *
 * @param context The context to load resources.
 */
internal open class DefaultChannelOptionItemsFactory(
    private val context: Context,
) : ChannelOptionItemsFactory {

    /**
     * Creates [ChannelAction]s for the selected channel.
     *
     * [ChannelAction.onAction] is left empty because the XML module dispatches
     * actions through [ChannelActionsDialogFragment.ChannelOptionClickListener],
     * not through the action's own handler.
     *
     * @param selectedChannel The currently selected channel.
     * @param ownCapabilities Set of capabilities the user is given for the current channel.
     * @param style The style of the dialog.
     * @return The list of channel actions to display.
     */
    override fun createChannelOptionItems(
        selectedChannel: Channel,
        ownCapabilities: Set<String>,
        style: ChannelActionsDialogViewStyle,
    ): List<ChannelAction> {
        val canLeaveChannel = ownCapabilities.contains(ChannelCapabilities.LEAVE_CHANNEL)
        val canDeleteChannel = ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)

        return listOfNotNull(
            if (style.viewInfoEnabled) {
                ViewInfo(
                    channel = selectedChannel,
                    label = context.getString(R.string.stream_ui_channel_list_view_info),
                    onAction = {},
                )
            } else {
                null
            },
            if (style.leaveGroupEnabled && canLeaveChannel) {
                LeaveGroup(
                    channel = selectedChannel,
                    label = context.getString(R.string.stream_ui_channel_list_leave_channel),
                    onAction = {},
                )
            } else {
                null
            },
            if (style.deleteConversationEnabled && canDeleteChannel) {
                DeleteConversation(
                    channel = selectedChannel,
                    label = context.getString(R.string.stream_ui_channel_list_delete_channel),
                    onAction = {},
                )
            } else {
                null
            },
        )
    }
}
