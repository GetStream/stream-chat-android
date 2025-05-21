/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState

@Composable
internal fun ChannelInfoMemberOption(
    option: ChannelInfoMemberViewState.Content.Option,
    onViewAction: (action: ChannelInfoMemberViewAction) -> Unit,
) {
    when (option) {
        is ChannelInfoMemberViewState.Content.Option.MessageMember -> {
            ChannelInfoOptionNavigationButton(
                icon = R.drawable.stream_compose_empty_channels,
                text = stringResource(R.string.stream_ui_channel_info_member_option_message_member),
                onClick = { onViewAction(ChannelInfoMemberViewAction.MemberMessageClick) },
            )
        }

        is ChannelInfoMemberViewState.Content.Option.BanMember -> {
            CompositionLocalProvider(LocalContentColor.provides(ChatTheme.colors.errorAccent)) {
                ChannelInfoOptionButton(
                    icon = R.drawable.stream_ic_ban,
                    text = stringResource(R.string.stream_ui_channel_info_member_option_ban_member),
                    onClick = { onViewAction(ChannelInfoMemberViewAction.BanMemberClick) },
                )
            }
        }

        is ChannelInfoMemberViewState.Content.Option.UnbanMember -> {
            CompositionLocalProvider(LocalContentColor.provides(ChatTheme.colors.errorAccent)) {
                ChannelInfoOptionButton(
                    icon = R.drawable.stream_ic_ban,
                    text = stringResource(R.string.stream_ui_channel_info_member_option_unban_member),
                    onClick = { onViewAction(ChannelInfoMemberViewAction.UnbanMemberClick) },
                )
            }
        }

        is ChannelInfoMemberViewState.Content.Option.RemoveMember -> {
            CompositionLocalProvider(LocalContentColor.provides(ChatTheme.colors.errorAccent)) {
                ChannelInfoOptionButton(
                    icon = R.drawable.stream_compose_ic_person_remove,
                    text = stringResource(R.string.stream_ui_channel_info_member_option_remove_member),
                    onClick = { onViewAction(ChannelInfoMemberViewAction.RemoveMemberClick) },
                )
            }
        }
    }
}
