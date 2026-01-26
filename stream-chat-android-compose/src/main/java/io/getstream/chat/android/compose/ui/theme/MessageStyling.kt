package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

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
}
