package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.list.internal.ScrollButtonView

/**
 * Style for [ScrollButtonView]
 *
 * @property scrollButtonEnabled - enables/disables view which allows to scroll to the latest messages. Default - enabled.
 * @property scrollButtonUnreadEnabled - enables/disables unread label. Default - enabled
 * @property scrollButtonColor - [ScrollButtonView] background color. Default - [R.color.stream_ui_white].
 * @property scrollButtonRippleColor - [ScrollButtonView] ripple color. Default - [R.color.stream_ui_white_smoke]
 * @property scrollButtonBadgeColor - unread label background color. Default - [R.color.stream_ui_accent_blue]
 * @property scrollButtonIcon - [ScrollButtonView] icon. Default - [R.drawable.stream_ui_ic_down]
 * @property scrollButtonBadgeTextStyle - appearance for unread label
 */
public data class ScrollButtonViewStyle(
    public val scrollButtonEnabled: Boolean,
    public val scrollButtonUnreadEnabled: Boolean,
    @ColorInt public val scrollButtonColor: Int,
    @ColorInt public val scrollButtonRippleColor: Int,
    @ColorInt public val scrollButtonBadgeColor: Int,
    public val scrollButtonIcon: Drawable?,
    public val scrollButtonBadgeTextStyle: TextStyle
) {

    internal class Builder(private val context: Context, private val attrs: TypedArray) {
        private var scrollButtonEnabled: Boolean = false
        private var scrollButtonUnreadEnabled: Boolean = false
        @ColorInt private var scrollButtonColor: Int = 0
        @ColorInt private var scrollButtonRippleColor: Int = 0
        @ColorInt private var scrollButtonBadgeColor: Int = 0
        private var scrollButtonIcon: Drawable? = null

        fun scrollButtonEnabled(
            @StyleableRes scrollButtonEnabledStyleableId: Int,
            defaultValue: Boolean,
        ) = apply {
            scrollButtonEnabled = attrs.getBoolean(scrollButtonEnabledStyleableId, defaultValue)
        }

        fun scrollButtonUnreadEnabled(
            @StyleableRes scrollButtonUnreadEnabledStyleableId: Int,
            defaultValue: Boolean,
        ) = apply {
            scrollButtonUnreadEnabled = attrs.getBoolean(scrollButtonUnreadEnabledStyleableId, defaultValue)
        }

        fun scrollButtonColor(
            @StyleableRes scrollButtonColorStyleableId: Int,
            @ColorInt defaultValue: Int,
        ) = apply {
            scrollButtonColor = attrs.getColor(scrollButtonColorStyleableId, defaultValue)
        }

        fun scrollButtonRippleColor(
            @StyleableRes scrollButtonRippleColorStyleableId: Int,
            @ColorInt defaultColor: Int,
        ) = apply {
            scrollButtonRippleColor = attrs.getColor(scrollButtonRippleColorStyleableId, defaultColor)
        }

        fun scrollButtonBadgeColor(
            @StyleableRes scrollButtonBadgeColorStyleableId: Int,
            @ColorInt defaultColor: Int,
        ) = apply {
            scrollButtonBadgeColor = attrs.getColor(scrollButtonBadgeColorStyleableId, defaultColor)
        }

        fun scrollButtonIcon(
            @StyleableRes scrollButtonIconStyleableId: Int,
            defaultIcon: Drawable?,
        ) = apply {
            scrollButtonIcon = attrs.getDrawable(scrollButtonIconStyleableId) ?: defaultIcon
        }

        fun build(): ScrollButtonViewStyle {
            val scrollButtonBadgeTextStyle = TextStyle.Builder(attrs)
                .size(
                    R.styleable.MessageListView_streamUiScrollButtonBadgeTextSize,
                    context.getDimension(R.dimen.stream_ui_scroll_button_unread_badge_text_size)
                )
                .color(
                    R.styleable.MessageListView_streamUiScrollButtonBadgeTextColor,
                    context.getColorCompat(R.color.stream_ui_literal_white)
                )
                .font(
                    R.styleable.MessageListView_streamUiScrollButtonBadgeFontAssets,
                    R.styleable.MessageListView_streamUiScrollButtonBadgeTextFont,
                )
                .style(R.styleable.MessageListView_streamUiScrollButtonBadgeTextStyle, Typeface.BOLD)
                .build()

            return ScrollButtonViewStyle(
                scrollButtonEnabled = scrollButtonEnabled,
                scrollButtonUnreadEnabled = scrollButtonUnreadEnabled,
                scrollButtonColor = scrollButtonColor,
                scrollButtonRippleColor = scrollButtonRippleColor,
                scrollButtonBadgeColor = scrollButtonBadgeColor,
                scrollButtonIcon = scrollButtonIcon,
                scrollButtonBadgeTextStyle = scrollButtonBadgeTextStyle
            ).let(TransformStyle.scrollButtonStyleTransformer::transform)
        }
    }
}
