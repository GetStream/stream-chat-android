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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.message.belongsToThread
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.DateFormatType
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.channels.MessageReadStatusIcon
import io.getstream.chat.android.compose.ui.components.messages.MessageThreadFooter
import io.getstream.chat.android.compose.ui.components.messages.MessageTranslatedLabel
import io.getstream.chat.android.compose.ui.components.messages.factory.MessageContentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

internal class AiMessageContentFactory : MessageContentFactory() {
    @Composable
    override fun MessageFooterContent(messageItem: MessageItemState) {
        val message = messageItem.message
        val alignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)

        if (message.belongsToThread() && !messageItem.isInThread) {
            val threadFooterText =
                when (message.replyCount) {
                    0 -> LocalContext.current.resources.getString(R.string.stream_compose_thread_reply)
                    else ->
                        LocalContext.current.resources.getQuantityString(
                            R.plurals.stream_compose_message_list_thread_footnote,
                            message.replyCount,
                            message.replyCount,
                        )
                }
            MessageThreadFooter(
                participants = message.threadParticipants,
                messageAlignment = alignment,
                text = threadFooterText,
            )
        }

        Column(horizontalAlignment = alignment.contentAlignment) {
            MessageTranslatedLabel(messageItem)
            if (messageItem.showMessageFooter) {
                Row(
                    modifier =
                    Modifier
                        .padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!messageItem.isMine) {
                        Text(
                            modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .weight(1f, fill = false)
                                .testTag("Stream_MessageAuthorName"),
                            text = message.user.name,
                            style = ChatTheme.typography.footnote,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = ChatTheme.colors.textLowEmphasis,
                        )
                    } else {
                        MessageReadStatusIcon(
                            modifier = Modifier.padding(end = 4.dp),
                            message = messageItem.message,
                            isMessageRead = messageItem.isMessageRead,
                            readCount = messageItem.messageReadBy.size,
                        )
                    }

                    val updatedAt = message.updatedAt
                    val createdAt = message.createdAt ?: message.createdLocallyAt
                    val date =
                        when {
                            createdAt == null -> updatedAt
                            updatedAt == null -> createdAt
                            else ->
                                when (updatedAt.after(createdAt)) {
                                    true -> updatedAt
                                    else -> createdAt
                                }
                        }
                    if (date != null) {
                        Timestamp(date = date, formatType = DateFormatType.TIME)
                    }
                }
            }
        }
    }
}
