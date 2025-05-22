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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
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
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.previewdata.PreviewMembersData
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent.BanMemberModal.Timeout

@Suppress("LongMethod")
@Composable
internal fun ChannelInfoMemberInfoConfirmationModal(
    modal: ChannelInfoMemberViewEvent.Modal?,
    onViewAction: (action: ChannelInfoMemberViewAction) -> Unit,
    onDismiss: () -> Unit,
) {
    when (modal) {
        is ChannelInfoMemberViewEvent.RemoveMemberModal -> {
            val user = modal.member.user
            SimpleDialog(
                title = stringResource(R.string.stream_ui_channel_info_member_option_remove_member),
                message = stringResource(
                    R.string.stream_ui_channel_info_member_option_remove_member_confirmation,
                    user.name.takeIf(String::isNotBlank) ?: user.id,
                ),
                onPositiveAction = {
                    onViewAction(ChannelInfoMemberViewAction.RemoveMemberConfirmationClick)
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        is ChannelInfoMemberViewEvent.BanMemberModal -> {
            val user = modal.member.user
            var selectedTimeout by remember { mutableStateOf(Timeout.OneHour) }

            SimpleDialog(
                title = stringResource(
                    R.string.stream_ui_channel_info_member_option_ban_modal_title,
                    user.name.takeIf(String::isNotBlank) ?: user.id,
                ),
                text = {
                    Column {
                        modal.timeouts.forEach { timeout ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTimeout = timeout },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = timeout == selectedTimeout,
                                    onClick = { selectedTimeout = timeout },
                                )
                                val label = stringResource(
                                    when (timeout) {
                                        Timeout.OneHour ->
                                            R.string.stream_ui_channel_info_member_option_ban_modal_timeout_one_hour

                                        Timeout.OneDay ->
                                            R.string.stream_ui_channel_info_member_option_ban_modal_timeout_one_day

                                        Timeout.OneWeek ->
                                            R.string.stream_ui_channel_info_member_option_ban_modal_timeout_one_week

                                        Timeout.NoTimeout ->
                                            R.string.stream_ui_channel_info_member_option_ban_modal_no_timeout
                                    },
                                )
                                Text(text = label)
                            }
                        }
                    }
                },
                onConfirmClick = {
                    onViewAction(
                        ChannelInfoMemberViewAction.BanMemberConfirmationClick(
                            timeoutInMinutes = selectedTimeout.valueInMinutes,
                        ),
                    )
                    onDismiss()
                },
                onDismiss = onDismiss,
            )
        }

        null -> Unit
    }
}

@Preview
@Composable
private fun ChannelInfoMemberInfoConfirmationModalBanPreview() {
    ChatTheme {
        ChannelInfoMemberInfoConfirmationModalBan()
    }
}

@Composable
internal fun ChannelInfoMemberInfoConfirmationModalBan() {
    ChannelInfoMemberInfoConfirmationModal(
        modal = ChannelInfoMemberViewEvent.BanMemberModal(
            member = PreviewMembersData.member1,
        ),
        onViewAction = {},
        onDismiss = {},
    )
}

@Preview
@Composable
private fun ChannelInfoMemberInfoConfirmationModalRemovePreview() {
    ChatTheme {
        ChannelInfoMemberInfoConfirmationModalRemove()
    }
}

@Composable
internal fun ChannelInfoMemberInfoConfirmationModalRemove() {
    ChannelInfoMemberInfoConfirmationModal(
        modal = ChannelInfoMemberViewEvent.RemoveMemberModal(
            member = PreviewMembersData.member1,
        ),
        onViewAction = {},
        onDismiss = {},
    )
}
