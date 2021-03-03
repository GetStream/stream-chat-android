package io.getstream.chat.android.ui.message.list.internal

import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import java.io.Serializable

public class MessageListItemStyle private constructor(
    @ColorInt public val messageBackgroundColorMine: Int?,
    @ColorInt public val messageBackgroundColorTheirs: Int?,
    @ColorInt public val messageTextColorMine: Int?,
    @ColorInt public val messageTextColorTheirs: Int?,
    @ColorInt public val messageLinkTextColorMine: Int?,
    @ColorInt public val messageLinkTextColorTheirs: Int?,
) : Serializable {

    @ColorInt
    public fun getStyleTextColor(isMine: Boolean): Int? {
        return if (isMine) messageTextColorMine else messageTextColorTheirs
    }

    @ColorInt
    public fun getStyleLinkTextColor(isMine: Boolean): Int? {
        return if (isMine) messageLinkTextColorMine else messageLinkTextColorTheirs
    }

    internal companion object {
        internal const val VALUE_NOT_SET = Integer.MAX_VALUE
    }

    internal class Builder(private val attributes: TypedArray) {
        @ColorInt
        private var messageBackgroundColorMine: Int = VALUE_NOT_SET

        @ColorInt
        private var messageBackgroundColorTheirs: Int = VALUE_NOT_SET

        @ColorInt
        private var messageTextColorMine: Int = VALUE_NOT_SET

        @ColorInt
        private var messageTextColorTheirs: Int = VALUE_NOT_SET

        @ColorInt
        private var messageLinkTextColorMine: Int = VALUE_NOT_SET

        @ColorInt
        private var messageLinkTextColorTheirs: Int = VALUE_NOT_SET

        fun messageBackgroundColorMine(
            @StyleableRes messageBackgroundColorMineStyleableId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageBackgroundColorMine = attributes.getColor(messageBackgroundColorMineStyleableId, defaultValue)
        }

        fun messageBackgroundColorTheirs(
            @StyleableRes messageBackgroundColorTheirsId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageBackgroundColorTheirs = attributes.getColor(messageBackgroundColorTheirsId, defaultValue)
        }

        fun messageTextColorMine(
            @StyleableRes messageTextColorMineId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageTextColorMine = attributes.getColor(messageTextColorMineId, defaultValue)
        }

        fun messageTextColorTheirs(
            @StyleableRes messageTextColorTheirsId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageTextColorTheirs = attributes.getColor(messageTextColorTheirsId, defaultValue)
        }

        fun messageLinkTextColorMine(
            @StyleableRes messageLinkTextColorMineId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageLinkTextColorMine = attributes.getColor(messageLinkTextColorMineId, defaultValue)
        }

        fun messageLinkTextColorTheirs(
            @StyleableRes messageLinkTextColorTheirsId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageLinkTextColorTheirs = attributes.getColor(messageLinkTextColorTheirsId, defaultValue)
        }

        fun build(): MessageListItemStyle {
            return MessageListItemStyle(
                messageBackgroundColorMine = messageBackgroundColorMine.nullIfNotSet(),
                messageBackgroundColorTheirs = messageBackgroundColorTheirs.nullIfNotSet(),
                messageTextColorMine = messageTextColorMine.nullIfNotSet(),
                messageTextColorTheirs = messageTextColorTheirs.nullIfNotSet(),
                messageLinkTextColorMine = messageLinkTextColorMine.nullIfNotSet(),
                messageLinkTextColorTheirs = messageLinkTextColorTheirs.nullIfNotSet(),
            )
        }

        private fun Int.nullIfNotSet(): Int? {
            return if (this == VALUE_NOT_SET) null else this
        }
    }
}
