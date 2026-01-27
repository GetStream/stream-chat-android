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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.ui.common.state.messages.list.MessagePosition

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
    fun timestampStyle(): TextStyle {
        return ChatTheme.typography.metadataDefault.copy(color = ChatTheme.colors.chatTextTimestamp)
    }

    private val roundBubble = RoundedCornerShape(StreamTokens.radius2xl)
    private val outgoingBubble = RoundedCornerShape(
        topStart = StreamTokens.radius2xl,
        topEnd = StreamTokens.radius2xl,
        bottomStart = StreamTokens.radius2xl,
        bottomEnd = ZeroCornerSize,
    )
    private val incomingBubble = RoundedCornerShape(
        topStart = StreamTokens.radius2xl,
        topEnd = StreamTokens.radius2xl,
        bottomStart = ZeroCornerSize,
        bottomEnd = StreamTokens.radius2xl,
    )

    fun shape(position: MessagePosition, outgoing: Boolean): Shape {
        return when (position) {
            MessagePosition.TOP,
            MessagePosition.MIDDLE,
                -> roundBubble

            MessagePosition.BOTTOM,
            MessagePosition.NONE,
                -> when {
                outgoing -> outgoingBubble
                else -> incomingBubble
            }
        }
    }
}
