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

package io.getstream.chat.android.compose.ui.channels.info

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.ui.components.StreamCardBottomSheet
import io.getstream.chat.android.compose.ui.theme.ChannelMenuCenterContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelMenuHeaderContentParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo

/**
 * Bottom sheet showing the available actions for a channel.
 *
 * @param channel The channel the actions apply to.
 * @param actions The list of actions to show.
 * @param onActionClick Invoked when the user clicks an action. Destructive actions route through
 * a separate confirmation step before executing.
 * @param onDismiss Invoked when the sheet is dismissed.
 * @param modifier Modifier applied to the sheet container.
 * @param currentUser The currently logged-in user. Used by the default header to derive inline
 * state icons (muted, pinned).
 * @param header The content shown at the top of the sheet.
 * @param content The content shown below the header.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ChannelActionsSheet(
    channel: Channel,
    actions: List<ChannelAction>,
    onActionClick: (ChannelAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    currentUser: User? = null,
    header: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMenuHeaderContent(
                params = ChannelMenuHeaderContentParams(
                    selectedChannel = channel,
                    currentUser = currentUser,
                ),
            )
        }
    },
    content: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMenuCenterContent(
                params = ChannelMenuCenterContentParams(
                    onChannelOptionConfirm = onActionClick,
                    channelActions = actions,
                ),
            )
        }
    },
) {
    StreamCardBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        header()
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelActionsSheetPreview() {
    ChatTheme {
        ChannelActionsSheetSample()
    }
}

@Composable
internal fun ChannelActionsSheetSample() {
    val channel = PreviewChannelData.channelWithManyMembers
    ChannelActionsSheet(
        channel = channel,
        actions = listOf(ViewInfo(channel = channel, label = "Channel Info", onAction = {})),
        onActionClick = {},
        onDismiss = {},
        currentUser = PreviewUserData.user1,
    )
}
