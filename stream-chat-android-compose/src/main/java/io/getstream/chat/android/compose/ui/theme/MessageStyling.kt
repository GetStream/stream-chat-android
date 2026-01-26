package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
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
    fun messageTextStyle(): TextStyle =
        messageTextStyle(ChatTheme.typography, ChatTheme.colors)

    fun messageTextStyle(typography: StreamTypography, colors: StreamColors): TextStyle =
        typography.bodyDefault.copy(color = colors.chatTextMessage)

    fun messageLinkStyle(typography: StreamTypography, colors: StreamColors): TextStyle =
        typography.bodyDefault.copy(color = colors.chatTextLink)

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
