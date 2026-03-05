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

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.state.channel.info.BanMember
import io.getstream.chat.android.ui.common.state.channel.info.BlockUser
import io.getstream.chat.android.ui.common.state.channel.info.MemberAction
import io.getstream.chat.android.ui.common.state.channel.info.MuteUser
import io.getstream.chat.android.ui.common.state.channel.info.RemoveMember
import io.getstream.chat.android.ui.common.state.channel.info.SendDirectMessage
import io.getstream.chat.android.ui.common.state.channel.info.UnbanMember
import io.getstream.chat.android.ui.common.state.channel.info.UnblockUser
import io.getstream.chat.android.ui.common.state.channel.info.UnmuteUser

/**
 * Builds the default list of member actions for the member info modal sheet.
 *
 * Each action is self-describing and carries its icon, label, and execution handler.
 * Actions are filtered by channel capabilities and current state (muted, blocked, banned).
 *
 * @param member The member targeted by the actions.
 * @param capabilities The set of own capabilities in the channel.
 * @param isMuted Whether the member's user is muted by the current user.
 * @param isBlocked Whether the member's user is blocked by the current user.
 * @param onViewAction Callback invoked when a view action is triggered.
 * @return The list of member actions to display.
 */
@Suppress("LongParameterList")
@Composable
public fun buildDefaultMemberActions(
    member: Member,
    capabilities: Set<String>,
    isMuted: Boolean,
    isBlocked: Boolean,
    onViewAction: (ChannelInfoMemberViewAction) -> Unit,
): List<MemberAction> {
    return listOfNotNull(
        SendDirectMessage(
            member = member,
            label = stringResource(R.string.stream_ui_channel_info_member_modal_option_message_member),
            onAction = { onViewAction(ChannelInfoMemberViewAction.MessageMemberClick) },
        ),
        buildMuteAction(member, isMuted, onViewAction),
        buildBlockAction(member, isBlocked, onViewAction),
        buildBanAction(
            canBan = capabilities.contains(ChannelCapabilities.BAN_CHANNEL_MEMBERS),
            member = member,
            onViewAction = onViewAction,
        ),
        if (capabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS)) {
            RemoveMember(
                member = member,
                label = stringResource(R.string.stream_ui_channel_info_member_modal_option_remove_member),
                onAction = { onViewAction(ChannelInfoMemberViewAction.RemoveMemberClick) },
            )
        } else {
            null
        },
    )
}

@Composable
private fun buildMuteAction(
    member: Member,
    isMuted: Boolean,
    onViewAction: (ChannelInfoMemberViewAction) -> Unit,
): MemberAction = if (isMuted) {
    UnmuteUser(
        member = member,
        label = stringResource(R.string.stream_ui_channel_info_member_modal_option_unmute_user),
        onAction = { onViewAction(ChannelInfoMemberViewAction.UnmuteUserClick) },
    )
} else {
    MuteUser(
        member = member,
        label = stringResource(R.string.stream_ui_channel_info_member_modal_option_mute_user),
        onAction = { onViewAction(ChannelInfoMemberViewAction.MuteUserClick) },
    )
}

@Composable
private fun buildBlockAction(
    member: Member,
    isBlocked: Boolean,
    onViewAction: (ChannelInfoMemberViewAction) -> Unit,
): MemberAction = if (isBlocked) {
    UnblockUser(
        member = member,
        label = stringResource(R.string.stream_ui_channel_info_member_modal_option_unblock_user),
        onAction = { onViewAction(ChannelInfoMemberViewAction.UnblockUserClick) },
    )
} else {
    BlockUser(
        member = member,
        label = stringResource(R.string.stream_ui_channel_info_member_modal_option_block_user),
        onAction = { onViewAction(ChannelInfoMemberViewAction.BlockUserClick) },
    )
}

@Composable
private fun buildBanAction(
    canBan: Boolean,
    member: Member,
    onViewAction: (ChannelInfoMemberViewAction) -> Unit,
): MemberAction? {
    if (!canBan) return null
    return if (member.banned) {
        UnbanMember(
            member = member,
            label = stringResource(R.string.stream_ui_channel_info_member_modal_option_unban_member),
            onAction = { onViewAction(ChannelInfoMemberViewAction.UnbanMemberClick) },
        )
    } else {
        BanMember(
            member = member,
            label = stringResource(R.string.stream_ui_channel_info_member_modal_option_ban_member),
            onAction = { onViewAction(ChannelInfoMemberViewAction.BanMemberClick) },
        )
    }
}
