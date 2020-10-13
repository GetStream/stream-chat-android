package com.getstream.sdk.chat.style

import androidx.annotation.FontRes

interface ChatStyle {

    fun hasDefaultFont(): Boolean

    fun getDefaultTextStyle(): TextStyle

    class Builder {
        var result = ChatStyleImpl()

        fun setDefaultFont(assetPath: String): Builder = apply {
            result.defaultTextStyle = TextStyle().apply {
                fontAssetsPath = assetPath
            }
        }

        fun setDefaultFont(@FontRes fontRes: Int): Builder = apply {
            result.defaultTextStyle = TextStyle().apply {
                fontResource = fontRes
            }
        }

        fun build(): ChatStyle = result
    }
}
