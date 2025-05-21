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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent

@Composable
internal fun ChannelInfoMemberInfoConfirmationModal(
    modal: ChannelInfoMemberViewEvent.Modal?,
    onViewAction: (action: ChannelInfoMemberViewAction) -> Unit,
    onDismiss: () -> Unit,
) {
    when (modal) {
        ChannelInfoMemberViewEvent.RemoveMemberModal -> {
            SimpleDialog(
                title = stringResource(R.string.stream_ui_channel_info_option_leave_group),
                message = stringResource(R.string.stream_ui_channel_info_option_leave_group_confirmation_message),
                onPositiveAction = {
                    onViewAction(ChannelInfoMemberViewAction.RemoveMemberConfirmationClick)
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
private fun ChannelInfoMemberInfoConfirmationModalRemovePreview() {
    ChatTheme {
        ChannelInfoMemberInfoConfirmationModal(
            modal = ChannelInfoMemberViewEvent.RemoveMemberModal,
            onViewAction = {},
            onDismiss = {},
        )
    }
}
