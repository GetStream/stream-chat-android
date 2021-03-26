package io.getstream.chat.android.ui.common.style

public class ChatStyle {
    public var defaultTextStyle: TextStyle? = null
    public fun hasDefaultFont(): Boolean = defaultTextStyle?.hasFont() == true
}
