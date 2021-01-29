package io.getstream.chat.android.ui.messages.view

import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes

internal class MessageListItemStyle private constructor(
    val messageColorMine: Int,
    val messageColorTheirs: Int,
) {

    internal class Builder(private val attributes: TypedArray? = null) {
        @ColorInt
        private var messageColorMine: Int = 0

        @ColorInt
        private var messageColorTheirs: Int = 0

        fun messageBackgroundColorMine(
            @StyleableRes messageColorMineStyleableId: Int,
            @ColorInt defaultValue: Int,
        ) = apply {
            messageColorMine = attributes?.getColor(messageColorMineStyleableId, defaultValue) ?: defaultValue
        }

        fun messageBackgroundColorTheirs(
            @StyleableRes messageColorTheirsStyleableId: Int = 0,
            @ColorInt defaultValue: Int,
        ) = apply {
            messageColorTheirs = attributes?.getColor(messageColorTheirsStyleableId, defaultValue) ?: defaultValue
        }

        fun build(): MessageListItemStyle {
            return MessageListItemStyle(
                messageColorMine,
                messageColorTheirs
            )
        }
    }
}
