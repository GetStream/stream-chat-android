package io.getstream.chat.android.ui.message.list.internal

import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes

internal class ScrollButtonViewStyle(
    val scrollButtonEnabled: Boolean,
    val scrollButtonUnreadEnabled: Boolean,
    val scrollButtonColor: Int,
    val scrollButtonRippleColor: Int,
    val scrollButtonBadgeColor: Int,
    val scrollButtonIcon: Drawable?
) {

    internal class Builder(private val a: TypedArray) {
        private var scrollButtonEnabled: Boolean = false
        private var scrollButtonUnreadEnabled: Boolean = false
        private var scrollButtonColor: Int = 0
        private var scrollButtonRippleColor: Int = 0
        private var scrollButtonBadgeColor: Int = 0
        private var scrollButtonIcon: Drawable? = null

        fun scrollButtonEnabled(
            @StyleableRes scrollButtonEnabledStyleableId: Int,
            defaultValue: Boolean
        ) = apply {
            scrollButtonEnabled = a.getBoolean(scrollButtonEnabledStyleableId, defaultValue)
        }

        fun scrollButtonUnreadEnabled(
            @StyleableRes scrollButtonUnreadEnabledStyleableId: Int,
            defaultValue: Boolean
        ) = apply {
            scrollButtonUnreadEnabled = a.getBoolean(scrollButtonUnreadEnabledStyleableId, defaultValue)
        }

        fun scrollButtonColor(
            @StyleableRes scrollButtonColorStyleableId: Int,
            @ColorInt defaultValue: Int
        ) = apply {
            scrollButtonColor = a.getColor(scrollButtonColorStyleableId, defaultValue)
        }

        fun scrollButtonRippleColor(
            @StyleableRes scrollButtonRippleColorStyleableId: Int,
            @ColorInt defaultColor: Int
        ) = apply {
            scrollButtonRippleColor = a.getColor(scrollButtonRippleColorStyleableId, defaultColor)
        }

        fun scrollButtonBadgeColor(
            @StyleableRes scrollButtonBadgeColorStyleableId: Int,
            @ColorInt defaultColor: Int
        ) = apply {
            scrollButtonBadgeColor = a.getColor(scrollButtonBadgeColorStyleableId, defaultColor)
        }

        fun scrollButtonIcon(
            @StyleableRes scrollButtonIconStyleableId: Int,
            defaultIcon: Drawable?
        ) = apply {
            scrollButtonIcon = a.getDrawable(scrollButtonIconStyleableId) ?: defaultIcon
        }

        fun build(): ScrollButtonViewStyle =
            ScrollButtonViewStyle(
                scrollButtonEnabled,
                scrollButtonUnreadEnabled,
                scrollButtonColor,
                scrollButtonRippleColor,
                scrollButtonBadgeColor,
                scrollButtonIcon
            )
    }
}
