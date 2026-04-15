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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isThreadStart
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.DateFormatType
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageFooterStatusIndicatorParams
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowMessageStatusIndicator

/**
 * Default message footer, which contains either [MessageThreadFooter] or the default footer, which
 * holds the sender name and the timestamp.
 *
 * @param messageItem Message to show.
 */
@Composable
@Suppress("LongMethod")
public fun MessageFooter(
    messageItem: MessageItemState,
) {
    val message = messageItem.message
    val alignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItem)

    Column(horizontalAlignment = alignment.contentAlignment) {
        if (message.isThreadStart() && !messageItem.isInThread) {
            val threadFooterText = when (message.replyCount) {
                0 -> stringResource(R.string.stream_compose_thread_reply)
                else -> pluralStringResource(
                    R.plurals.stream_compose_message_list_thread_footnote,
                    message.replyCount,
                    message.replyCount,
                )
            }
            val threadFooterTextColor = if (messageItem.isPreviewMode) {
                ChatTheme.colors.textOnAccent
            } else {
                ChatTheme.colors.chatTextLink
            }
            MessageThreadFooter(
                participants = message.threadParticipants,
                messageAlignment = alignment,
                text = threadFooterText,
                textColor = threadFooterTextColor,
            )
        }

        val showEditLabel = message.messageTextUpdatedAt != null && !message.isDeleted()
        val timestampStyle = MessageStyling.timestampStyle()

        if (messageItem.showMessageFooter) {
            Row(
                modifier = Modifier.padding(top = StreamTokens.spacingXs, bottom = StreamTokens.spacing2xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!messageItem.isMine) {
                    Text(
                        modifier = Modifier
                            .clearAndSetSemantics {
                                testTag = "Stream_MessageAuthorName"
                            }
                            .padding(end = StreamTokens.spacingXs)
                            .weight(1f, fill = false),
                        text = message.user.name,
                        style = ChatTheme.typography.metadataEmphasis,
                        color = ChatTheme.colors.chatTextUsername,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                } else if (message.shouldShowMessageStatusIndicator()) {
                    ChatTheme.componentFactory.MessageFooterStatusIndicator(
                        params = MessageFooterStatusIndicatorParams(
                            modifier = Modifier.padding(end = StreamTokens.spacing2xs),
                            messageItem = messageItem,
                        ),
                    )
                }

                val date = message.getCreatedAtOrNull()
                if (date != null) {
                    Timestamp(
                        date = date,
                        formatType = DateFormatType.TIME,
                        textStyle = timestampStyle,
                    )
                }
                if (showEditLabel) {
                    Text(
                        modifier = Modifier
                            .padding(start = StreamTokens.spacingXs)
                            .testTag("Stream_MessageEditedLabel"),
                        text = LocalContext.current.getString(R.string.stream_compose_message_list_footnote_edited),
                        style = timestampStyle,
                    )
                }
            }
        } else if (showEditLabel) {
            Row(
                modifier = Modifier.padding(top = StreamTokens.spacingXs, bottom = StreamTokens.spacing2xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.testTag("Stream_MessageEditedLabel"),
                    text = LocalContext.current.getString(R.string.stream_compose_message_list_footnote_edited),
                    style = timestampStyle,
                )
            }
        }
    }
}
