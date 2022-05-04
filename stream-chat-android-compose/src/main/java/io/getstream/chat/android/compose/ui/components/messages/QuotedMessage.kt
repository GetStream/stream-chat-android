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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Wraps the quoted message into a special component, that doesn't show some information, like
 * the timestamp, thread participants and similar.
 *
 * @param message Message to show.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the item is long clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun QuotedMessage(
    message: Message,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit,
    onQuotedMessageClick: (Message) -> Unit,
) {
    val user = message.user

    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Avatar(
            modifier = Modifier.size(24.dp),
            imageUrl = user.image,
            initials = user.initials,
            textStyle = ChatTheme.typography.captionBold,
        )

        Spacer(modifier = Modifier.size(8.dp))

        MessageBubble(
            modifier = Modifier.combinedClickable(true) {
                println("clicked")
                onQuotedMessageClick(message)
            },
            shape = ChatTheme.shapes.otherMessageBubble, color = ChatTheme.colors.barsBackground,
            content = {
                Column {
                    MessageAttachmentsContent(
                        message = message,
                        onLongItemClick = {}
                    )

                    if (message.text.isNotEmpty()) {
                        MessageText(
                            isQuote = true,
                            message = message,
                            onLongItemClick = onLongItemClick
                        )
                    }
                }
            }
        )
    }
}
