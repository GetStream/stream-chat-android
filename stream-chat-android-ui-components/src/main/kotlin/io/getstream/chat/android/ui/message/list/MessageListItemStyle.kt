package io.getstream.chat.android.ui.message.list

import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import io.getstream.chat.android.ui.TransformStyle
import java.io.Serializable

public data class MessageListItemStyle(
    @ColorInt public val messageBackgroundColorMine: Int?,
    @ColorInt public val messageBackgroundColorTheirs: Int?,
    @ColorInt public val messageTextColorMine: Int?,
    @ColorInt public val messageTextColorTheirs: Int?,
    @ColorInt public val messageLinkTextColorMine: Int?,
    @ColorInt public val messageLinkTextColorTheirs: Int?,
    public val reactionsEnabled: Boolean,
    public val threadsEnabled: Boolean,
    public val linkDescriptionMaxLines: Int,
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

        private var reactionsEnabled: Boolean = true
        private var threadsEnabled: Boolean = true

        private var linkDescriptionMaxLines: Int = 5

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

        fun reactionsEnabled(
            @StyleableRes reactionsEnabled: Int,
            defaultValue: Boolean = true,
        ) = apply {
            this.reactionsEnabled = attributes.getBoolean(reactionsEnabled, defaultValue)
        }

        fun threadsEnabled(
            @StyleableRes threadsEnabled: Int,
            defaultValue: Boolean = true,
        ) = apply {
            this.threadsEnabled = attributes.getBoolean(threadsEnabled, defaultValue)
        }

        fun linkDescriptionMaxLines(
            maxLines: Int,
            defaultValue: Int = 5,
        ) = apply {
            this.linkDescriptionMaxLines = attributes.getInt(maxLines, defaultValue)
        }

        fun build(): MessageListItemStyle {
            return MessageListItemStyle(
                messageBackgroundColorMine = messageBackgroundColorMine.nullIfNotSet(),
                messageBackgroundColorTheirs = messageBackgroundColorTheirs.nullIfNotSet(),
                messageTextColorMine = messageTextColorMine.nullIfNotSet(),
                messageTextColorTheirs = messageTextColorTheirs.nullIfNotSet(),
                messageLinkTextColorMine = messageLinkTextColorMine.nullIfNotSet(),
                messageLinkTextColorTheirs = messageLinkTextColorTheirs.nullIfNotSet(),
                reactionsEnabled = reactionsEnabled,
                threadsEnabled = threadsEnabled,
                linkDescriptionMaxLines = linkDescriptionMaxLines
            ).let(TransformStyle.messageListItemStyleTransformer::transform)
        }

        private fun Int.nullIfNotSet(): Int? {
            return if (this == VALUE_NOT_SET) null else this
        }
    }
}
