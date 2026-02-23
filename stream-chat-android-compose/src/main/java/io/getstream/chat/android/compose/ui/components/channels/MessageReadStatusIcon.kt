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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.extensions.deliveredReadsOf
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
    val isMessageDelivered = channel.deliveredReadsOf(message).isNotEmpty()

    MessageReadStatusIcon(
        modifier = modifier,
        message = message,
        isMessageRead = isMessageRead,
        isMessageDelivered = isMessageDelivered,
        readCount = readCount,
    )
}

/**
 * Shows a delivery status indicator for a particular message.
 *
 * @param message The message with sync status to check.
 * @param isMessageRead If the message is read by any member.
 * @param isMessageDelivered If the message is delivered to any member.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MessageReadStatusIcon(
    message: Message,
    isMessageRead: Boolean,
    modifier: Modifier = Modifier,
    isMessageDelivered: Boolean = false,
    readCount: Int = 0,
    isReadIcon: @Composable () -> Unit = { IsReadCount(modifier = modifier, readCount = readCount) },
    isPendingIcon: @Composable () -> Unit = { IsPendingIcon(modifier = modifier) },
    isSentIcon: @Composable () -> Unit = { IsSentIcon(modifier = modifier) },
    isDeliveredIcon: @Composable () -> Unit = { IsDeliveredIcon(modifier = modifier) },
) {
    val syncStatus = message.syncStatus

    when (syncStatus) {
        SyncStatus.IN_PROGRESS,
        SyncStatus.SYNC_NEEDED,
        SyncStatus.AWAITING_ATTACHMENTS,
        -> isPendingIcon()

        SyncStatus.COMPLETED -> when {
            isMessageRead -> isReadIcon()
            isMessageDelivered -> isDeliveredIcon()
            else -> isSentIcon()
        }

        SyncStatus.FAILED_PERMANENTLY -> IsErrorIcon(modifier = modifier)
    }
}

@Composable
private fun IsReadCount(
    modifier: Modifier,
    readCount: Int,
) {
    val showReadCount = readCount > 1 && ChatTheme.readCountEnabled
    val description = if (showReadCount) {
        stringResource(R.string.stream_ui_message_list_semantics_message_status_read_by, readCount)
    } else {
        stringResource(R.string.stream_ui_message_list_semantics_message_status_read)
    }
    Row(
        modifier = modifier
            .semantics { contentDescription = description }
            .padding(start = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (showReadCount) {
            Text(
                text = readCount.toString(),
                modifier = Modifier.testTag("Stream_MessageReadCount"),
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.chatTextTimestamp,
            )
        }
        Icon(
            modifier = Modifier.testTag("Stream_MessageReadStatus_isRead"),
            painter = painterResource(id = R.drawable.stream_compose_message_seen),
            contentDescription = null,
            tint = ChatTheme.colors.accentPrimary,
        )
    }
}

@Composable
private fun IsPendingIcon(modifier: Modifier) {
    Icon(
        modifier = modifier.testTag("Stream_MessageReadStatus_isPending"),
        painter = painterResource(id = R.drawable.stream_compose_ic_clock),
        contentDescription = stringResource(R.string.stream_ui_message_list_semantics_message_status_pending),
        tint = ChatTheme.colors.chatTextTimestamp,
    )
}

@Composable
private fun IsSentIcon(modifier: Modifier) {
    Icon(
        modifier = modifier.testTag("Stream_MessageReadStatus_isSent"),
        painter = painterResource(id = R.drawable.stream_compose_message_sent),
        contentDescription = stringResource(R.string.stream_ui_message_list_semantics_message_status_sent),
        tint = ChatTheme.colors.chatTextTimestamp,
    )
}

@Composable
private fun IsErrorIcon(modifier: Modifier) {
    Icon(
        modifier = modifier.testTag("Stream_MessageReadStatus_isError"),
        painter = painterResource(id = R.drawable.stream_compose_ic_error),
        contentDescription = null,
        tint = ChatTheme.colors.accentError,
    )
}

@Composable
private fun IsDeliveredIcon(modifier: Modifier) {
    Icon(
        modifier = modifier.testTag("Stream_MessageReadStatus_isDelivered"),
        painter = painterResource(id = R.drawable.stream_compose_message_seen),
        contentDescription = stringResource(
            R.string.stream_ui_message_list_semantics_message_status_delivered,
        ),
        tint = ChatTheme.colors.chatTextTimestamp,
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
