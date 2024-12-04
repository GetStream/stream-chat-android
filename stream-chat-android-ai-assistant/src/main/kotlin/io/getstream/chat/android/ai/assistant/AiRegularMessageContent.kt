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

package io.getstream.chat.android.ai.assistant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isErrorOrFailed
import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.components.messages.MessageBubble
import io.getstream.chat.android.compose.ui.components.messages.MessageContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition

@Composable
public fun AiRegularMessageContent(
    messageItem: MessageItemState,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit = {},
    onGiphyActionClick: (GiphyAction) -> Unit = {},
    onQuotedMessageClick: (Message) -> Unit = {},
    onAnimationState: (Boolean) -> Unit,
    onLinkClick: ((Message, String) -> Unit)? = null,
    typingState: TypingState,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val message = messageItem.message
    val position = messageItem.groupPosition
    val ownsMessage = messageItem.isMine

    val messageBubbleShape = when {
        position.contains(MessagePosition.TOP) || position.contains(MessagePosition.MIDDLE) -> RoundedCornerShape(
            16.dp,
        )

        else -> {
            if (ownsMessage) ChatTheme.shapes.myMessageBubble else ChatTheme.shapes.otherMessageBubble
        }
    }

    val messageBubbleColor = when {
        message.isGiphyEphemeral() -> ChatTheme.colors.giphyMessageBackground
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
                MessageContent(
                    message = message,
                    currentUser = messageItem.currentUser,
                    onLongItemClick = onLongItemClick,
                    onGiphyActionClick = onGiphyActionClick,
                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                    onQuotedMessageClick = onQuotedMessageClick,
                    onLinkClick = onLinkClick,
                    regularMessageContent = {
                        DefaultMessageTextContent(
                            message = message,
                            typingState = typingState,
                            currentUser = messageItem.currentUser,
                            onLongItemClick = onLongItemClick,
                            onLinkClick = onLinkClick,
                            onAnimationState = onAnimationState,
                        )
                    },
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
                        onGiphyActionClick = onGiphyActionClick,
                        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                        onQuotedMessageClick = onQuotedMessageClick,
                        onLinkClick = onLinkClick,
                        regularMessageContent = {
                            DefaultMessageTextContent(
                                message = message,
                                typingState = typingState,
                                onAnimationState = onAnimationState,
                                currentUser = messageItem.currentUser,
                                onLongItemClick = onLongItemClick,
                                onLinkClick = onLinkClick,
                            )
                        },
                    )
                },
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(BottomEnd),
                painter = painterResource(id = io.getstream.chat.android.compose.R.drawable.stream_compose_ic_error),
                contentDescription = null,
                tint = ChatTheme.colors.errorAccent,
            )
        }
    }
}

/**
 * The default text message content. It holds the quoted message in case there is one.
 *
 * @param message The message to show.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onLinkClick Handler for link clicks.
 */
@Composable
internal fun DefaultMessageTextContent(
    message: Message,
    currentUser: User?,
    typingState: TypingState,
    onAnimationState: (Boolean) -> Unit,
    onLongItemClick: (Message) -> Unit,
    onLinkClick: ((Message, String) -> Unit)? = null,
) {
    AiMessageText(
        message = message,
        currentUser = currentUser,
        onLongItemClick = onLongItemClick,
        onLinkClick = onLinkClick,
        typingState = typingState,
        onAnimationState = onAnimationState,
    )
}

/**
 * @return If the current message failed to send.
 */
@OptIn(InternalStreamChatApi::class)
internal fun MessageItemState.isErrorOrFailed(): Boolean = isMine && message.isErrorOrFailed()
