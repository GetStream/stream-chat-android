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

package io.getstream.chat.android.compose.ui.components.messages.factory

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageDeletedContent
import io.getstream.chat.android.compose.ui.components.messages.DefaultMessageGiphyContent
import io.getstream.chat.android.compose.ui.components.messages.MessageFooter
import io.getstream.chat.android.compose.ui.components.messages.MessageText
import io.getstream.chat.android.compose.ui.components.messages.QuotedMessage
import io.getstream.chat.android.compose.ui.components.messages.UploadingFooter
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

/**
 * Factory for creating message contents that are used to represent the chat message items.
 */
@Deprecated(
    message = "This class is deprecated and will be removed in the future. " +
        "Please use ChatComponentFactory and provide it to the ChatTheme instead.",
    level = DeprecationLevel.WARNING,
)
public open class MessageContentFactory {

    public companion object {
        public val Deprecated: MessageContentFactory = MessageContentFactory()
    }

    /**
     * Represents the default Giphy message content.
     */
    @Composable
    public open fun MessageGiphyContent(
        message: Message,
        onGiphyActionClick: (GiphyAction) -> Unit,
    ) {
        DefaultMessageGiphyContent(
            message = message,
            onGiphyActionClick = onGiphyActionClick,
        )
    }

    /**
     * Represents the default content of a deleted message.
     */
    @Composable
    public open fun MessageDeletedContent(
        modifier: Modifier,
    ) {
        DefaultMessageDeletedContent(modifier = modifier)
    }

    /**
     * Represents the default regular message content.
     */
    @Composable
    public open fun MessageTextContent(
        message: Message,
        currentUser: User?,
        onLongItemClick: (Message) -> Unit,
        onLinkClick: ((Message, String) -> Unit)?,
        onUserMentionClick: (User) -> Unit,
    ) {
        MessageText(
            message = message,
            currentUser = currentUser,
            onLongItemClick = onLongItemClick,
            onLinkClick = onLinkClick,
            onUserMentionClick = onUserMentionClick,
        )
    }

    /**
     * Represents the default quoted message content.
     */
    @Composable
    public open fun QuotedMessageContent(
        message: Message,
        currentUser: User?,
        onLongItemClick: (Message) -> Unit,
        onQuotedMessageClick: (Message) -> Unit,
    ) {
        val quotedMessage = message.replyTo
        if (quotedMessage != null) {
            QuotedMessage(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                message = quotedMessage,
                currentUser = currentUser,
                replyMessage = message,
                onLongItemClick = { onLongItemClick(message) },
                onQuotedMessageClick = onQuotedMessageClick,
            )
        }
    }

    @Composable
    public open fun UploadingFooterContent(
        modifier: Modifier,
        messageItem: MessageItemState,
    ) {
        val message = messageItem.message
        UploadingFooter(
            modifier = modifier,
            message = message,
        )
    }

    /**
     * Represents the default message visibility content.
     */
    @Composable
    public open fun OwnedMessageVisibilityContent(
        messageItem: MessageItemState,
    ) {
        val message = messageItem.message
        io.getstream.chat.android.compose.ui.components.messages.OwnedMessageVisibilityContent(message = message)
    }

    /**
     * Represents the default message footer.
     */
    @Composable
    public open fun MessageFooterContent(
        messageItem: MessageItemState,
    ) {
        MessageFooter(messageItem = messageItem)
    }
}
