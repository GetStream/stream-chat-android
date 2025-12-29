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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.attachments.content.QuotedMessageAttachmentContent
import io.getstream.chat.android.compose.ui.components.CancelIcon
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.isMine

/**
 * Wraps the quoted message into a component that shows only the sender avatar, text and single attachment preview.
 *
 * @param message The quoted message to show.
 * @param currentUser The currently logged in user.
 * @param onLongItemClick Handler when the item is long clicked.
 * @param onQuotedMessageClick Handler for quoted message click action.
 * @param modifier Modifier for styling.
 * @param replyMessage The message that contains the reply.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun QuotedMessage(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    replyMessage: Message? = null,
) {
    // We base the "own message" check on:
    // - `replyMessage` if not null, i.e. we're composing a reply
    // - `message` otherwise, i.e. we're rendering an already-sent message
    val style =
        if ((replyMessage ?: message).isMine(currentUser)) {
            ChatTheme.ownMessageTheme.quoted
        } else {
            ChatTheme.otherMessageTheme.quoted
        }
    val backgroundColor =
        if (replyMessage == null) {
            style.backgroundColorInComposer
        } else {
            style.backgroundColor
        }

    Row(
        modifier = modifier
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onLongClick = { onLongItemClick(message) },
                onClick = { onQuotedMessageClick(message) },
            )
            .background(backgroundColor, style.backgroundShape)
            .padding(style.contentPadding)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 2.dp,
            color = style.indicatorColor,
        )

        Column(
            Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
        ) {
            // TODO [G.] Apply correct text styles
            // TODO [G.] Move to resources
            val userName = if (message.isMine(currentUser)) {
                "You"
            } else if (replyMessage == null) {
                "Reply to ${message.user.name}"
            } else {
                message.user.name
            }

            Text(userName, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)

            message.text.takeUnless(String::isBlank)?.let { text ->
                Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            message.attachments
                .firstOrNull()
                ?.let { it.titleLink ?: it.ogUrl }
                ?.let { text ->
                    Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
        }

        if (message.attachments.isNotEmpty()) {
            QuotedMessageAttachmentContent(
                message = message,
                currentUser = currentUser,
                onLongItemClick = {},
            )
        }
    }
}

// TODO [G.] public?
@Composable
public fun MessageComposerQuotedMessage(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit,
) {
    Box {
        QuotedMessage(message, currentUser, {}, {})

        CancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(4.dp, (-4).dp),
            onClick = onCancelClick,
        )
    }
}
