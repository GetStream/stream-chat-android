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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChannelInfoScreenModal(
    modal: ChannelInfoViewEvent.Modal?,
    isGroupChannel: Boolean,
    onViewAction: (action: ChannelInfoViewAction) -> Unit,
    onDismiss: () -> Unit,
) {
    when (modal) {
        is ChannelInfoViewEvent.MemberInfoModal -> {
            ModalBottomSheet(
                containerColor = ChatTheme.colors.barsBackground,
                onDismissRequest = onDismiss,
            ) {
                MemberInfoSheet(user = modal.user)
            }
        }

        ChannelInfoViewEvent.HideChannelModal -> {
            var clearHistory by remember { mutableStateOf(false) }
            SimpleDialog(
                title = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_hide_group)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_hide_conversation)
                },
                text = {
                    HideChannelModalText(
                        isGroupChannel = isGroupChannel,
                        clearHistory = clearHistory,
                        onClearHistoryClick = { clearHistory = !clearHistory },
                    )
                },
                onConfirmClick = {
                    onViewAction(ChannelInfoViewAction.HideChannelConfirmationClick(clearHistory = clearHistory))
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        ChannelInfoViewEvent.DeleteChannelModal -> {
            SimpleDialog(
                title = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_delete_group)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_delete_conversation)
                },
                message = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_delete_group_confirmation)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_delete_conversation_confirmation)
                },
                onPositiveAction = {
                    onViewAction(ChannelInfoViewAction.DeleteChannelConfirmationClick)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        ChannelInfoViewEvent.LeaveChannelModal -> {
            SimpleDialog(
                title = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_leave_group)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_leave_conversation)
                },
                message = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_leave_group_confirmation)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_leave_conversation_confirmation)
                },
                onPositiveAction = {
                    // TODO Get quit message configuration from ChatTheme
                    onViewAction(ChannelInfoViewAction.LeaveChannelConfirmationClick(quitMessage = null))
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        null -> Unit
    }
}

@Composable
private fun MemberInfoSheet(user: User) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChannelInfoMemberInfo(user = user)
        ChannelInfoOptionButton(
            icon = R.drawable.stream_compose_empty_channels,
            text = stringResource(R.string.stream_ui_channel_info_option_pinned_messages),
            onClick = {
                // TODO Copy user handle
            },
        )
    }
}

@Composable
private fun HideChannelModalText(
    isGroupChannel: Boolean,
    clearHistory: Boolean,
    onClearHistoryClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (isGroupChannel) {
                stringResource(R.string.stream_ui_channel_info_option_hide_group_confirmation)
            } else {
                stringResource(R.string.stream_ui_channel_info_option_hide_conversation_confirmation)
            },
            color = ChatTheme.colors.textHighEmphasis,
            style = ChatTheme.typography.body,
        )
        Row(
            modifier = Modifier
                .clickable(onClick = onClearHistoryClick)
                .padding(end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = clearHistory,
                onCheckedChange = null,
            )
            Text(
                text = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_hide_group_confirmation_clear_history)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_hide_conversation_confirmation_clear_history)
                },
                color = ChatTheme.colors.textLowEmphasis,
                style = ChatTheme.typography.body,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelInfoScreenModalMemberPreview() {
    ChatTheme {
        MemberInfoSheet(user = PreviewUserData.user1)
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalHideChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.HideChannelModal,
            isGroupChannel = false,
            onViewAction = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalLeaveChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.LeaveChannelModal,
            isGroupChannel = false,
            onViewAction = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalDeleteChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.DeleteChannelModal,
            isGroupChannel = false,
            onViewAction = {},
            onDismiss = {},
        )
    }
}
