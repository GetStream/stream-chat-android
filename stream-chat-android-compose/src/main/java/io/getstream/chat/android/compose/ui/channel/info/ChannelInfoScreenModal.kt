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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent

@Composable
internal fun ChannelInfoScreenModal(
    modal: ChannelInfoViewEvent.Modal?,
    isGroupChannel: Boolean,
    onViewAction: (action: ChannelInfoViewAction) -> Unit,
    onDismiss: () -> Unit,
) {
    when (modal) {
        ChannelInfoViewEvent.HideChannelModal -> {
            SimpleDialog(
                title = if (isGroupChannel) {
                    stringResource(R.string.stream_ui_channel_info_option_hide_group)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_hide_conversation)
                },
                text = { HideChannelModalText(isGroupChannel) },
                onConfirmClick = {
                    onViewAction(ChannelInfoViewAction.HideChannelConfirmationClick(clearHistory = false))
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
                    stringResource(R.string.stream_ui_channel_info_option_delete_group_confirmation_message)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_delete_conversation_confirmation_message)
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
                    stringResource(R.string.stream_ui_channel_info_option_leave_group_confirmation_message)
                } else {
                    stringResource(R.string.stream_ui_channel_info_option_leave_conversation_confirmation_message)
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
private fun HideChannelModalText(isGroupChannel: Boolean) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (isGroupChannel) {
                stringResource(R.string.stream_ui_channel_info_option_hide_group_confirmation_message)
            } else {
                stringResource(R.string.stream_ui_channel_info_option_hide_conversation_confirmation_message)
            },
            color = ChatTheme.colors.textHighEmphasis,
            style = ChatTheme.typography.body,
        )
        var clearHistory by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .clickable { clearHistory = !clearHistory }
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
