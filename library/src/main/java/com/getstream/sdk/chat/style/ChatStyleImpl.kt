package com.getstream.sdk.chat.style;

public class ChatStyleImpl implements ChatStyle {

    TextStyle defaultTextStyle;

    public TextStyle getDefaultTextStyle() {
        return defaultTextStyle;
    }

    @Override
    public boolean hasDefaultFont() {
        return defaultTextStyle != null && defaultTextStyle.hasFont();
    }

}