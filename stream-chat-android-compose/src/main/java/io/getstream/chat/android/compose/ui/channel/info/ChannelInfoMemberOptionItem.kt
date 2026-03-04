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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.common.MenuOptionItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState

@Composable
internal fun ChannelInfoMemberOptionItem(
    option: ChannelInfoMemberViewState.Content.Option,
    onViewAction: (action: ChannelInfoMemberViewAction) -> Unit,
) {
    when (option) {
        is ChannelInfoMemberViewState.Content.Option.MessageMember -> {
            MemberMenuOptionItem(
                icon = R.drawable.stream_ic_chat_bubble,
                text = stringResource(R.string.stream_ui_channel_info_member_modal_option_message_member),
                isDestructive = false,
                onClick = { onViewAction(ChannelInfoMemberViewAction.MessageMemberClick) },
            )
        }

        is ChannelInfoMemberViewState.Content.Option.BanMember -> {
            MemberMenuOptionItem(
                icon = R.drawable.stream_ic_ban,
                text = stringResource(R.string.stream_ui_channel_info_member_modal_option_ban_member),
                isDestructive = true,
                onClick = { onViewAction(ChannelInfoMemberViewAction.BanMemberClick) },
            )
        }

        is ChannelInfoMemberViewState.Content.Option.UnbanMember -> {
            MemberMenuOptionItem(
                icon = R.drawable.stream_ic_ban,
                text = stringResource(R.string.stream_ui_channel_info_member_modal_option_unban_member),
                isDestructive = true,
                onClick = { onViewAction(ChannelInfoMemberViewAction.UnbanMemberClick) },
            )
        }

        is ChannelInfoMemberViewState.Content.Option.RemoveMember -> {
            MemberMenuOptionItem(
                icon = R.drawable.stream_ic_action_leave,
                text = stringResource(R.string.stream_ui_channel_info_member_modal_option_remove_member),
                isDestructive = true,
                onClick = { onViewAction(ChannelInfoMemberViewAction.RemoveMemberClick) },
            )
        }
    }
}

@Composable
private fun MemberMenuOptionItem(
    @DrawableRes icon: Int,
    text: String,
    isDestructive: Boolean,
    onClick: () -> Unit,
) {
    val titleColor = if (isDestructive) {
        ChatTheme.colors.accentError
    } else {
        ChatTheme.colors.textPrimary
    }
    val iconColor = if (isDestructive) {
        ChatTheme.colors.accentError
    } else {
        ChatTheme.colors.textSecondary
    }
    MenuOptionItem(
        modifier = Modifier.padding(horizontal = StreamTokens.spacingMd),
        title = text,
        titleColor = titleColor,
        leadingIcon = {
            MemberMenuOptionLeadingIcon(icon = icon, tint = iconColor)
        },
        onClick = onClick,
        style = ChatTheme.typography.bodyDefault,
        itemHeight = 44.dp,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    )
}

@Composable
private fun MemberMenuOptionLeadingIcon(
    @DrawableRes icon: Int,
    tint: Color,
) {
    Icon(
        modifier = Modifier
            .padding(end = StreamTokens.spacingXs)
            .size(StreamTokens.spacingXl),
        painter = painterResource(id = icon),
        tint = tint,
        contentDescription = null,
    )
}
