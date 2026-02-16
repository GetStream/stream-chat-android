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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition
import io.getstream.chat.android.ui.common.utils.extensions.isMine

internal object MessageStyling {
    @Composable
    fun backgroundColor(outgoing: Boolean, colors: StreamColors = ChatTheme.colors) = when {
        outgoing -> colors.chatBgOutgoing
        else -> colors.chatBgIncoming
    }

    @Composable
    fun textColor(outgoing: Boolean): Color {
        return textColor(outgoing, ChatTheme.colors)
    }

    fun textColor(outgoing: Boolean, colors: StreamColors): Color {
        return if (outgoing) {
            colors.chatTextOutgoing
        } else {
            colors.chatTextIncoming
        }
    }

    @Composable
    fun textStyle(outgoing: Boolean): TextStyle =
        textStyle(outgoing, ChatTheme.typography, ChatTheme.colors)

    fun textStyle(outgoing: Boolean, typography: StreamTypography, colors: StreamColors): TextStyle {
        return typography.bodyDefault.copy(color = textColor(outgoing, colors))
    }

    fun linkStyle(typography: StreamTypography, colors: StreamColors): TextStyle =
        typography.bodyDefault.copy(color = colors.chatTextLink)

    @Composable
    fun attachmentBackgroundColor(outgoing: Boolean): Color = when {
        outgoing -> ChatTheme.colors.chatBgAttachmentOutgoing
        else -> ChatTheme.colors.chatBgAttachmentIncoming
    }

    @Composable
    fun timestampStyle(): TextStyle {
        return ChatTheme.typography.metadataDefault.copy(color = ChatTheme.colors.chatTextTimestamp)
    }

    @Composable
    fun quotedMessageStyle(message: Message, replyMessage: Message?, currentUser: User?): QuotedMessageStyle {
        val colors = ChatTheme.colors

        return if (replyMessage != null) {
            // replyMessage is not null: we're rendering an already-sent message
            if (replyMessage.isMine(currentUser)) {
                QuotedMessageStyle(
                    backgroundColor = colors.chatBgAttachmentOutgoing,
                    indicatorColor = colors.chatReplyIndicatorOutgoing,
                    textColor = colors.chatTextOutgoing,
                )
            } else {
                QuotedMessageStyle(
                    backgroundColor = colors.chatBgAttachmentIncoming,
                    indicatorColor = colors.chatReplyIndicatorIncoming,
                    textColor = colors.chatTextIncoming,
                )
            }
        } else {
            // replyMessage is null: we're composing a reply
            if (message.isMine(currentUser)) {
                QuotedMessageStyle(
                    backgroundColor = colors.chatBgOutgoing,
                    indicatorColor = colors.chatReplyIndicatorOutgoing,
                    textColor = colors.chatTextOutgoing,
                )
            } else {
                QuotedMessageStyle(
                    backgroundColor = colors.chatBgIncoming,
                    indicatorColor = colors.chatReplyIndicatorIncoming,
                    textColor = colors.chatTextIncoming,
                )
            }
        }
    }

    val contentPadding = StreamTokens.spacingXs
    val sectionsDistance = StreamTokens.spacingXs
    val messageSectionPadding = PaddingValues(
        top = sectionsDistance,
        start = contentPadding,
        end = contentPadding,
    )
    val textPadding = PaddingValues(
        top = sectionsDistance,
        start = StreamTokens.spacingSm,
        end = StreamTokens.spacingSm,
    )

    private val roundBubble = RoundedCornerShape(StreamTokens.radius2xl)
    private val rightPointingBubble = RoundedCornerShape(
        topStart = StreamTokens.radius2xl,
        topEnd = StreamTokens.radius2xl,
        bottomStart = StreamTokens.radius2xl,
        bottomEnd = ZeroCornerSize,
    )
    private val leftPointingBubble = RoundedCornerShape(
        topStart = StreamTokens.radius2xl,
        topEnd = StreamTokens.radius2xl,
        bottomStart = ZeroCornerSize,
        bottomEnd = StreamTokens.radius2xl,
    )

    fun shape(
        position: MessagePosition,
        messageAlignment: MessageAlignment,
    ): Shape {
        return when (position) {
            MessagePosition.TOP,
            MessagePosition.MIDDLE,
            -> roundBubble

            MessagePosition.BOTTOM,
            MessagePosition.NONE,
            -> when (messageAlignment) {
                MessageAlignment.End -> rightPointingBubble
                MessageAlignment.Start -> leftPointingBubble
            }
        }
    }

    data class QuotedMessageStyle(
        val backgroundColor: Color,
        val indicatorColor: Color,
        val textColor: Color,
    )
}
