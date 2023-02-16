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

import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewMessageData
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getReadStatuses
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.getCreatedAtOrThrow

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
    val readStatues = channel.getReadStatuses(userToIgnore = currentUser)
    val readCount = readStatues.count { it.time >= message.getCreatedAtOrThrow().time }
    val isMessageRead = readCount != 0

    MessageReadStatusIcon(
        message = message,
        isMessageRead = isMessageRead,
        modifier = modifier,
        readCount = readCount
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
) {
    val syncStatus = message.syncStatus

    when {
        isMessageRead -> {
            if (readCount > 1) {
                TextField(
                    value = readCount.toString(),
                    onValueChange = { },
                    singleLine = true,
                    modifier = modifier
                )
            }

            Icon(
                modifier = modifier,
                painter = painterResource(id = R.drawable.stream_compose_message_seen),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )
        }
        syncStatus == SyncStatus.SYNC_NEEDED || syncStatus == SyncStatus.AWAITING_ATTACHMENTS -> {
            Icon(
                modifier = modifier,
                painter = painterResource(id = R.drawable.stream_compose_ic_clock),
                contentDescription = null,
                tint = ChatTheme.colors.textLowEmphasis,
            )
        }
        syncStatus == SyncStatus.COMPLETED -> {
            Icon(
                modifier = modifier,
                painter = painterResource(id = R.drawable.stream_compose_message_sent),
                contentDescription = null,
                tint = ChatTheme.colors.textLowEmphasis,
            )
        }
    }
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
            readCount = 3
        )
    }
}
