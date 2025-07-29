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

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState

@Suppress("LongMethod")
@Composable
internal fun LazyItemScope.ChannelInfoOptionItem(
    option: ChannelInfoViewState.Content.Option,
    isGroupChannel: Boolean,
    onViewAction: (action: ChannelInfoViewAction) -> Unit,
) {
    when (option) {
        is ChannelInfoViewState.Content.Option.Separator -> {
            with(ChatTheme.componentFactory) {
                ChannelInfoSeparatorItem()
            }
        }

        is ChannelInfoViewState.Content.Option.AddMember -> {
            // Not rendered as an option, but as a button in the top bar
        }

        is ChannelInfoViewState.Content.Option.UserInfo -> {
            val user = option.user
            ChannelInfoUserInfoOption(
                username = user.name.takeIf(String::isNotBlank) ?: user.id,
                onClick = { onViewAction(ChannelInfoViewAction.UserInfoClick(user)) },
            )
        }

        is ChannelInfoViewState.Content.Option.RenameChannel -> {
            ChannelInfoNameField(
                name = option.name,
                readOnly = option.isReadOnly,
                onConfirmRenaming = { name ->
                    onViewAction(ChannelInfoViewAction.RenameChannelClick(name))
                },
            )
        }

        is ChannelInfoViewState.Content.Option.MuteChannel -> {
            // The LLC might take a few milliseconds to update the mute state.
            // So update UI immediately to avoid flickering.
            var muted by remember(option) { mutableStateOf(option.isMuted) }

            ChannelInfoOptionSwitch(
                icon = R.drawable.stream_compose_ic_mute,
                text = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_mute_group)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_mute_conversation)
                },
                checked = muted,
                onCheckedChange = { checked ->
                    muted = checked
                    if (muted) {
                        onViewAction(ChannelInfoViewAction.MuteChannelClick)
                    } else {
                        onViewAction(ChannelInfoViewAction.UnmuteChannelClick)
                    }
                },
            )
        }

        is ChannelInfoViewState.Content.Option.HideChannel -> {
            ChannelInfoOptionSwitch(
                icon = R.drawable.stream_ic_hide,
                text = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_hide_group)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_hide_conversation)
                },
                checked = option.isHidden,
                onCheckedChange = { checked ->
                    if (checked) {
                        onViewAction(ChannelInfoViewAction.HideChannelClick)
                    } else {
                        onViewAction(ChannelInfoViewAction.UnhideChannelClick)
                    }
                },
            )
        }

        is ChannelInfoViewState.Content.Option.PinnedMessages -> {
            ChannelInfoOptionNavigationButton(
                icon = R.drawable.stream_compose_ic_message_pinned,
                text = stringResource(R.string.stream_ui_channel_info_option_pinned_messages),
                onClick = { onViewAction(ChannelInfoViewAction.PinnedMessagesClick) },
            )
        }

        is ChannelInfoViewState.Content.Option.MediaAttachments -> {
            ChannelInfoOptionNavigationButton(
                icon = R.drawable.stream_compose_ic_image_picker,
                text = stringResource(R.string.stream_ui_channel_info_option_media_attachments),
                onClick = { onViewAction(ChannelInfoViewAction.MediaAttachmentsClick) },
            )
        }

        is ChannelInfoViewState.Content.Option.FilesAttachments -> {
            ChannelInfoOptionNavigationButton(
                icon = R.drawable.stream_compose_ic_file_picker,
                text = stringResource(R.string.stream_ui_channel_info_option_files_attachments),
                onClick = { onViewAction(ChannelInfoViewAction.FilesAttachmentsClick) },
            )
        }

        is ChannelInfoViewState.Content.Option.LeaveChannel -> {
            CompositionLocalProvider(LocalContentColor.provides(ChatTheme.colors.errorAccent)) {
                ChannelInfoOptionButton(
                    icon = R.drawable.stream_compose_ic_person_remove,
                    text = if (isGroupChannel) {
                        stringResource(R.string.stream_ui_channel_info_option_leave_group)
                    } else {
                        stringResource(R.string.stream_ui_channel_info_option_leave_conversation)
                    },
                    onClick = { onViewAction(ChannelInfoViewAction.LeaveChannelClick) },
                )
            }
        }

        is ChannelInfoViewState.Content.Option.DeleteChannel -> {
            CompositionLocalProvider(LocalContentColor.provides(ChatTheme.colors.errorAccent)) {
                ChannelInfoOptionButton(
                    icon = R.drawable.stream_compose_ic_delete,
                    text = if (isGroupChannel) {
                        stringResource(R.string.stream_ui_channel_info_option_delete_group)
                    } else {
                        stringResource(R.string.stream_ui_channel_info_option_delete_conversation)
                    },
                    onClick = { onViewAction(ChannelInfoViewAction.DeleteChannelClick) },
                )
            }
        }
    }
}
