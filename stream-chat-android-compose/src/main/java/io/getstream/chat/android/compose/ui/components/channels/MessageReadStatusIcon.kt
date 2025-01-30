/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getReadStatuses
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMessageData

/**
 * Shows a delivery status indicator for a particular message.
 *
 * @param channel The channel with channel reads to check.
 * @param message The message with sync status to check.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageReadStatusIcon(
    channel: Channel,
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val readStatuses = channel.getReadStatuses(userToIgnore = currentUser)
    val readCount = readStatuses.count { it.time >= message.getCreatedAtOrThrow().time }
    val isMessageRead = readCount != 0

    MessageReadStatusIcon(
        modifier = modifier,
        message = message,
        isMessageRead = isMessageRead,
        readCount = readCount,
    )
}

/**
 * Shows a delivery status indicator for a particular message.
 *
 * @param message The message with sync status to check.
 * @param isMessageRead If the message is read by any member.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageReadStatusIcon(
    message: Message,
    isMessageRead: Boolean,
    modifier: Modifier = Modifier,
    readCount: Int = 0,
    isReadIcon: @Composable () -> Unit = { IsReadCount(modifier = modifier, readCount = readCount) },
    isPendingIcon: @Composable () -> Unit = { IsPendingIcon(modifier = modifier) },
    isSentIcon: @Composable () -> Unit = { IsSentIcon(modifier = modifier) },
) {
    val syncStatus = message.syncStatus
    when {
        isMessageRead -> isReadIcon()

        syncStatus == SyncStatus.SYNC_NEEDED ||
            syncStatus == SyncStatus.AWAITING_ATTACHMENTS -> isPendingIcon()

        syncStatus == SyncStatus.COMPLETED -> isSentIcon()
    }
}

@Composable
private fun IsReadCount(
    modifier: Modifier,
    readCount: Int,
) {
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (readCount > 1 && ChatTheme.readCountEnabled) {
            Text(
                text = readCount.toString(),
                modifier = Modifier.testTag("Stream_MessageReadCount"),
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }
        Icon(
            modifier = Modifier.testTag("Stream_MessageReadStatus_isRead"),
            painter = painterResource(id = R.drawable.stream_compose_message_seen),
            contentDescription = null,
            tint = ChatTheme.colors.primaryAccent,
        )
    }
}

@Composable
private fun IsPendingIcon(modifier: Modifier) {
    Icon(
        modifier = modifier.testTag("Stream_MessageReadStatus_isPending"),
        painter = painterResource(id = R.drawable.stream_compose_ic_clock),
        contentDescription = null,
        tint = ChatTheme.colors.textLowEmphasis,
    )
}

@Composable
private fun IsSentIcon(modifier: Modifier) {
    Icon(
        modifier = modifier.testTag("Stream_MessageReadStatus_isSent"),
        painter = painterResource(id = R.drawable.stream_compose_message_sent),
        contentDescription = null,
        tint = ChatTheme.colors.textLowEmphasis,
    )
}

/**
 * Preview of [MessageReadStatusIcon] for a seen message.
 *
 * Should show a double tick indicator.
 */
@Preview(showBackground = true, name = "MessageReadStatusIcon Preview (Seen message)")
@Composable
private fun SeenMessageReadStatusIcon() {
    ChatTheme {
        MessageReadStatusIcon(
            message = PreviewMessageData.message2,
            isMessageRead = true,
            readCount = 3,
        )
    }
}
