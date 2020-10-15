package com.getstream.sdk.chat.style

class ChatStyleImpl : ChatStyle {
    internal var defaultTextStyle: TextStyle? = null

    override fun hasDefaultFont(): Boolean = defaultTextStyle?.hasFont() == true

    override fun getDefaultTextStyle(): TextStyle = defaultTextStyle!!
}
