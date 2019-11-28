package com.getstream.sdk.chat.style;

public class StreamChatStyleImpl implements StreamChatStyle {

    TextStyle defaultTextStyle;

    public TextStyle getDefaultTextStyle() {
        return defaultTextStyle;
    }

    @Override
    public boolean hasDefaultFont() {
        return defaultTextStyle != null && defaultTextStyle.hasFont();
    }

}