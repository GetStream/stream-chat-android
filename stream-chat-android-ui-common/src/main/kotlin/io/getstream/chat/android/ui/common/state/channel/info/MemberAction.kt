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

package io.getstream.chat.android.ui.common.state.channel.info

import androidx.annotation.DrawableRes
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.common.R

/**
 * A self-describing member action that carries display info and an execution handler.
 * Follows the same pattern as [io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction].
 *
 * @property member The member this action targets.
 * @property icon Drawable resource for the action icon.
 * @property label Human-readable label for the action.
 * @property isDestructive Whether this action is destructive (e.g. ban, remove).
 * @property onAction The handler to execute when the action is confirmed.
 */
public interface MemberAction {
    public val member: Member

    @get:DrawableRes
    public val icon: Int
    public val label: String
    public val isDestructive: Boolean get() = false
    public val onAction: () -> Unit
}

/**
 * Send a direct message to the member.
 */
public class SendDirectMessage(
    override val member: Member,
    override val label: String,
    override val onAction: () -> Unit,
) : MemberAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_chat_bubble
}

/**
 * Mute the member.
 */
public class MuteUser(
    override val member: Member,
    override val label: String,
    override val onAction: () -> Unit,
) : MemberAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_mute
}

/**
 * Unmute the member.
 */
public class UnmuteUser(
    override val member: Member,
    override val label: String,
    override val onAction: () -> Unit,
) : MemberAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_mute
}

/**
 * Block the member.
 */
public class BlockUser(
    override val member: Member,
    override val label: String,
    override val onAction: () -> Unit,
) : MemberAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_block
}

/**
 * Unblock the member.
 */
public class UnblockUser(
    override val member: Member,
    override val label: String,
    override val onAction: () -> Unit,
) : MemberAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_block
}

/**
 * Ban the member from the channel.
 */
public class BanMember(
    override val member: Member,
    override val label: String,
    override val onAction: () -> Unit,
) : MemberAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_ban
    override val isDestructive: Boolean = true
}

/**
 * Unban the member from the channel.
 */
public class UnbanMember(
    override val member: Member,
    override val label: String,
    override val onAction: () -> Unit,
) : MemberAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_ban
    override val isDestructive: Boolean = true
}

/**
 * Remove the member from the channel.
 */
public class RemoveMember(
    override val member: Member,
    override val label: String,
    override val onAction: () -> Unit,
) : MemberAction {
    @DrawableRes
    override val icon: Int = R.drawable.stream_ic_action_leave
    override val isDestructive: Boolean = true
}
