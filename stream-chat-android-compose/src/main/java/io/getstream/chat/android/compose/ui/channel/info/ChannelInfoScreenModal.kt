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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import io.getstream.chat.android.previewdata.PreviewMembersData
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent.BanMemberModal.Timeout

@Suppress("LongMethod")
@Composable
internal fun ChannelInfoScreenModal(
    modal: ChannelInfoViewEvent.Modal?,
    isGroupChannel: Boolean,
    onViewAction: (action: ChannelInfoViewAction) -> Unit,
    onMemberViewEvent: (event: ChannelInfoMemberViewEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    when (modal) {
        is ChannelInfoViewEvent.MemberInfoModal -> {
            ChannelInfoMemberInfoModalSheet(
                modal = modal,
                onMemberViewEvent = { event ->
                    onMemberViewEvent(event)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
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
                    onViewAction(ChannelInfoViewAction.HideChannelConfirmationClick(clearHistory))
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
                    stringResource(R.string.stream_ui_channel_info_delete_group_modal_message)
                } else {
                    stringResource(R.string.stream_ui_channel_info_delete_conversation_modal_message)
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
                    stringResource(R.string.stream_ui_channel_info_leave_group_modal_message)
                } else {
                    stringResource(R.string.stream_ui_channel_info_leave_conversation_modal_message)
                },
                onPositiveAction = {
                    // TODO Get quit message configuration from ChatTheme
                    onViewAction(ChannelInfoViewAction.LeaveChannelConfirmationClick(quitMessage = null))
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        is ChannelInfoViewEvent.BanMemberModal -> {
            var selectedTimeout by remember { mutableStateOf(Timeout.OneHour) }
            val member = modal.member

            SimpleDialog(
                title = stringResource(
                    R.string.stream_ui_channel_info_ban_member_modal_title,
                    member.user.name.takeIf(String::isNotBlank) ?: member.user.id,
                ),
                text = {
                    BanMemberModalText(
                        modal = modal,
                        selectedTimeout = selectedTimeout,
                        onTimeoutClick = { selectedTimeout = it },
                    )
                },
                onConfirmClick = {
                    onViewAction(
                        ChannelInfoViewAction.BanMemberConfirmationClick(
                            memberId = member.getUserId(),
                            timeoutInMinutes = selectedTimeout.valueInMinutes,
                        ),
                    )
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        is ChannelInfoViewEvent.RemoveMemberModal -> {
            val member = modal.member
            SimpleDialog(
                title = stringResource(R.string.stream_ui_channel_info_member_modal_option_remove_member),
                message = stringResource(
                    R.string.stream_ui_channel_info_remove_member_modal_message,
                    member.user.name.takeIf(String::isNotBlank) ?: member.getUserId(),
                ),
                onPositiveAction = {
                    onViewAction(ChannelInfoViewAction.RemoveMemberConfirmationClick(memberId = member.getUserId()))
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        null -> Unit
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
                stringResource(R.string.stream_ui_channel_info_hide_group_modal_message)
            } else {
                stringResource(R.string.stream_ui_channel_info_hide_conversation_modal_message)
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
                    stringResource(R.string.stream_ui_channel_info_hide_group_modal_clear_history)
                } else {
                    stringResource(R.string.stream_ui_channel_info_hide_conversation_modal_clear_history)
                },
                color = ChatTheme.colors.textLowEmphasis,
                style = ChatTheme.typography.body,
            )
        }
    }
}

@Composable
private fun BanMemberModalText(
    modal: ChannelInfoViewEvent.BanMemberModal,
    selectedTimeout: Timeout,
    onTimeoutClick: (timeout: Timeout) -> Unit,
) {
    Column {
        modal.timeouts.forEach { timeout ->
            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .fillMaxWidth()
                    .clickable(onClick = { onTimeoutClick(timeout) })
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    modifier = Modifier.padding(end = 8.dp),
                    selected = timeout == selectedTimeout,
                    onClick = null,
                )
                val label = stringResource(
                    when (timeout) {
                        Timeout.OneHour ->
                            R.string.stream_ui_channel_info_ban_member_modal_timeout_one_hour

                        Timeout.OneDay ->
                            R.string.stream_ui_channel_info_ban_member_modal_timeout_one_day

                        Timeout.OneWeek ->
                            R.string.stream_ui_channel_info_ban_member_modal_timeout_one_week

                        Timeout.NoTimeout ->
                            R.string.stream_ui_channel_info_ban_member_modal_no_timeout
                    },
                )
                Text(text = label)
            }
        }
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalHideDirectChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.HideChannelModal,
            isGroupChannel = false,
        )
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalHideGroupChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.HideChannelModal,
            isGroupChannel = true,
        )
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalLeaveDirectChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.LeaveChannelModal,
            isGroupChannel = false,
        )
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalLeaveGroupChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.LeaveChannelModal,
            isGroupChannel = true,
        )
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalDeleteDirectChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.DeleteChannelModal,
            isGroupChannel = false,
        )
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalDeleteGroupChannelPreview() {
    ChatTheme {
        ChannelInfoScreenModal(
            modal = ChannelInfoViewEvent.DeleteChannelModal,
            isGroupChannel = true,
        )
    }
}

@Preview
@Composable
private fun ChannelInfoScreenModalBanMemberPreview() {
    ChatTheme {
        ChannelInfoScreenModalBanMember()
    }
}

@Composable
internal fun ChannelInfoScreenModalBanMember() {
    ChannelInfoScreenModal(
        modal = ChannelInfoViewEvent.BanMemberModal(
            member = PreviewMembersData.member1,
        ),
        isGroupChannel = true,
    )
}

@Preview
@Composable
private fun ChannelInfoScreenModalRemoveMemberPreview() {
    ChatTheme {
        ChannelInfoScreenModalRemoveMember()
    }
}

@Composable
internal fun ChannelInfoScreenModalRemoveMember() {
    ChannelInfoScreenModal(
        modal = ChannelInfoViewEvent.RemoveMemberModal(
            member = PreviewMembersData.member1,
        ),
        isGroupChannel = true,
    )
}

@Composable
internal fun ChannelInfoScreenModal(
    modal: ChannelInfoViewEvent.Modal,
    isGroupChannel: Boolean,
) {
    ChatTheme.componentFactory.ChannelInfoScreenModal(
        modal = modal,
        isGroupChannel = isGroupChannel,
        onViewAction = {},
        onMemberViewEvent = {},
        onDismiss = {},
    )
}
