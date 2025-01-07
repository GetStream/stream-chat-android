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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isFewEmoji
import io.getstream.chat.android.compose.ui.util.isSingleEmoji
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.isMine

/**
 * Default text element for quoted messages, with extra styling and padding for the chat bubble.
 *
 * @param message Message to show.
 * @param currentUser The currently logged in user.
 * @param modifier Modifier for styling.
 * @param replyMessage The message that contains the reply.
 * @param quoteMaxLines Max number of lines quoted text can have.
 */
@Composable
public fun QuotedMessageText(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    replyMessage: Message? = null,
    quoteMaxLines: Int = DefaultQuoteMaxLines,
) {
    // TODO: Fix emoji font padding once this is resolved and exposed: https://issuetracker.google.com/issues/171394808
    val style = when {
        message.isSingleEmoji() -> ChatTheme.typography.singleEmoji
        message.isFewEmoji() -> ChatTheme.typography.emojiOnly
        else -> when (replyMessage?.isMine(currentUser) != false) {
            true -> ChatTheme.ownMessageTheme.quotedTextStyle
            else -> ChatTheme.otherMessageTheme.quotedTextStyle
        }
    }

    val styledText = ChatTheme.quotedMessageTextFormatter.format(message, replyMessage, currentUser)

    val horizontalPadding = ChatTheme.dimens.quotedMessageTextHorizontalPadding
    val verticalPadding = ChatTheme.dimens.quotedMessageTextVerticalPadding

    Text(
        modifier = modifier
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding,
            )
            .clipToBounds(),
        text = styledText,
        style = style,
        maxLines = quoteMaxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * The max length of quote message. After that it gets ellipsized.
 */
private const val DefaultQuoteMaxLines: Int = 3
