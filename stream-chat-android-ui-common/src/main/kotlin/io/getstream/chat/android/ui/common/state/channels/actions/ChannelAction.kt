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

package io.getstream.chat.android.ui.common.state.channels.actions

import androidx.annotation.DrawableRes
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.R

/**
 * A self-describing channel action that carries display info, capability requirements, and an
 * execution handler. Works for swipe actions, options menus, and any future action surface.
 *
 * @property channel The channel this action targets.
 * @property icon Drawable resource for the action icon.
 * @property label Human-readable label for the action.
 * @property requiredCapability Optional channel capability required to show this action.
 * @property confirmationPopup Optional confirmation dialog to show before executing.
 * @property isDestructive Whether this action is destructive (e.g. delete).
 * @property onAction The handler to execute when the action is confirmed.
 */
public interface ChannelAction {
    public val channel: Channel

    @get:DrawableRes
    public val icon: Int
    public val label: String
    public val requiredCapability: String? get() = null
    public val confirmationPopup: ConfirmationPopup? get() = null
    public val isDestructive: Boolean get() = false
    public val onAction: () -> Unit
}

/**
 * Show more info about the channel.
 */
public class ViewInfo(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_view_info
}

/**
 * Leave the group channel.
 */
public class LeaveGroup(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
    override val confirmationPopup: ConfirmationPopup? = null,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_leave
    override val requiredCapability: String = ChannelCapabilities.LEAVE_CHANNEL
}

/**
 * Mute the channel.
 */
public class MuteChannel(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_mute
    override val requiredCapability: String = ChannelCapabilities.MUTE_CHANNEL
}

/**
 * Unmute the channel.
 */
public class UnmuteChannel(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_unmute
    override val requiredCapability: String = ChannelCapabilities.MUTE_CHANNEL
}

/**
 * Delete the conversation.
 */
public class DeleteConversation(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
    override val confirmationPopup: ConfirmationPopup? = null,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_delete
    override val requiredCapability: String = ChannelCapabilities.DELETE_CHANNEL
    override val isDestructive: Boolean = true
}

/**
 * Pin the channel.
 */
public class PinChannel(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_pin
}

/**
 * Unpin the channel.
 */
public class UnpinChannel(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_unpin
}

/**
 * Archive the channel.
 */
public class ArchiveChannel(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_archive
}

/**
 * Unarchive the channel.
 */
public class UnarchiveChannel(
    override val channel: Channel,
    override val label: String,
    override val onAction: () -> Unit,
) : ChannelAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_unarchive
}
