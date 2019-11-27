package com.getstream.sdk.chat.style;

import androidx.annotation.FontRes;

public interface StreamChatStyle {

    TextStyle getDefaultTextStyle();

    boolean hasDefaultFont();

    class Builder {

        StreamChatStyleImpl result = new StreamChatStyleImpl();
        TextStyle defaultTextStyle;

        public Builder setDefaultFont(String assetPath) {
            defaultTextStyle = new TextStyle();
            defaultTextStyle.fontAssetsPath = assetPath;
            return this;
        }

        public Builder setDefaultFont(@FontRes int fontRes) {
            defaultTextStyle = new TextStyle();
            defaultTextStyle.fontResource = fontRes;
            return this;
        }

        public StreamChatStyle build() {
            result.defaultTextStyle = defaultTextStyle;
            return result;
        }
    }
}
