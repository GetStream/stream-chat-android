/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewMessageData
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isErrorOrFailed
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition

/**
 * Message content for the poll, which distinguishes the owner and users and allows them to interact.
 *
 * @param messageItem The message item to show the content for.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the user selects a message, on long tap.
 */
@Composable
public fun PollMessageContent(
    modifier: Modifier,
    messageItem: MessageItemState,
    onLongItemClick: (Message) -> Unit = {},
) {
    val message = messageItem.message
    val position = messageItem.groupPosition
    val ownsMessage = messageItem.isMine

    val messageBubbleShape = when {
        position.contains(MessagePosition.TOP) || position.contains(MessagePosition.MIDDLE) -> RoundedCornerShape(16.dp)
        else -> {
            if (ownsMessage) ChatTheme.shapes.myMessageBubble else ChatTheme.shapes.otherMessageBubble
        }
    }

    val messageBubbleColor = when {
        message.isDeleted() -> when (ownsMessage) {
            true -> ChatTheme.ownMessageTheme.deletedBackgroundColor
            else -> ChatTheme.otherMessageTheme.deletedBackgroundColor
        }

        else -> when (ownsMessage) {
            true -> ChatTheme.ownMessageTheme.backgroundColor
            else -> ChatTheme.otherMessageTheme.backgroundColor
        }
    }

    if (!messageItem.isErrorOrFailed()) {
        MessageBubble(
            modifier = modifier,
            shape = messageBubbleShape,
            color = messageBubbleColor,
            border = if (messageItem.isMine) null else BorderStroke(1.dp, ChatTheme.colors.borders),
            content = {
                Text(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    ),
                    text = message.poll.toString(),
                    color = ChatTheme.colors.errorAccent,
                )
            },
        )
    } else {
        Box(modifier = modifier) {
            MessageBubble(
                modifier = Modifier.padding(end = 12.dp),
                shape = messageBubbleShape,
                color = messageBubbleColor,
                content = {
                    MessageContent(
                        message = message,
                        currentUser = messageItem.currentUser,
                        onLongItemClick = onLongItemClick,
                        onGiphyActionClick = {},
                        onMediaGalleryPreviewResult = {},
                        onQuotedMessageClick = {},
                    )
                },
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd),
                painter = painterResource(id = R.drawable.stream_compose_ic_error),
                contentDescription = null,
                tint = ChatTheme.colors.errorAccent,
            )
        }
    }
}

@Preview
@Composable
private fun PollMessageContentPreview() {
    ChatTheme {
        Column {
            PollMessageContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                messageItem = MessageItemState(
                    message = PreviewMessageData.message1,
                    isMine = true,
                ),
            )

            PollMessageContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                messageItem = MessageItemState(
                    message = PreviewMessageData.messageWithError,
                    isMine = true,
                ),
            )
        }
    }
}
