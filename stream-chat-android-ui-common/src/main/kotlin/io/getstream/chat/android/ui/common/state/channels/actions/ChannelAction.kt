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

package io.getstream.chat.android.ui.common.state.channels.actions

import io.getstream.chat.android.models.Channel

/**
 * Represents the list of actions users can take with selected channels.
 *
 * @property channel The selected channel.
 */
public sealed class ChannelAction {
    public abstract val channel: Channel
}

/**
 * Show more info about the channel.
 */
public data class ViewInfo(override val channel: Channel) : ChannelAction()

/**
 * Shows a dialog to leave the group.
 */
public data class LeaveGroup(override val channel: Channel) : ChannelAction()

/**
 * Mutes the channel.
 */
public data class MuteChannel(override val channel: Channel) : ChannelAction()

/**
 * Unmutes the channel.
 */
public data class UnmuteChannel(override val channel: Channel) : ChannelAction()

/**
 * Shows a dialog to delete the conversation, if we have the permission.
 */
public data class DeleteConversation(override val channel: Channel) : ChannelAction()

/**
 * Shows a dialog to pin the channel.
 */
public data class PinChannel(override val channel: Channel) : ChannelAction()

/**
 * Shows a dialog to unpin the channel.
 */
public data class UnpinChannel(override val channel: Channel) : ChannelAction()

/**
 * Shows a dialog to archive the channel.
 */
public data class ArchiveChannel(override val channel: Channel) : ChannelAction()

/**
 * Shows a dialog to unarchive the channel.
 */
public data class UnarchiveChannel(override val channel: Channel) : ChannelAction()

/**
 * Dismisses the actions.
 */
public object Cancel : ChannelAction() {
    override val channel: Channel = Channel()
    override fun toString(): String = "Cancel"
}
