package com.getstream.sdk.chat.style

public class ChatStyle {
    public var defaultTextStyle: TextStyle? = null
    public fun hasDefaultFont(): Boolean = defaultTextStyle?.hasFont() == true
}
