package com.getstream.sdk.chat.style

import androidx.annotation.FontRes

public interface ChatStyle {

    public fun hasDefaultFont(): Boolean

    public fun getDefaultTextStyle(): TextStyle

    public class Builder {
        private var result = ChatStyleImpl()

        public fun setDefaultFont(assetPath: String): Builder = apply {
            result.defaultTextStyle = TextStyle().apply {
                fontAssetsPath = assetPath
            }
        }

        public fun setDefaultFont(@FontRes fontRes: Int): Builder = apply {
            result.defaultTextStyle = TextStyle().apply {
                fontResource = fontRes
            }
        }

        public fun build(): ChatStyle = result
    }
}
