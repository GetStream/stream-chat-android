package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use

public data class ChannelListViewStyle(
    public val optionsIcon: Drawable,
    public val deleteIcon: Drawable,
    public val optionsEnabled: Boolean,
    public val deleteEnabled: Boolean,
    public val swipeEnabled: Boolean,
    public val backgroundLayoutColor: Int,
    public val channelTitleText: TextStyle,
    public val lastMessageText: TextStyle,
    public val lastMessageDateText: TextStyle,
    public val indicatorSentIcon: Drawable,
    public val indicatorReadIcon: Drawable,
    public val indicatorPendingSyncIcon: Drawable,
    public val foregroundLayoutColor: Int,
    public val unreadMessageCounterText: TextStyle,
    public val unreadMessageCounterBackgroundColor: Int,
) {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): ChannelListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ChannelListView,
                0,
                0
            ).use { a ->
                val optionsIcon = a.getDrawable(R.styleable.ChannelListView_streamUiChannelOptionsIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_more)!!

                val deleteIcon = a.getDrawable(R.styleable.ChannelListView_streamUiChannelDeleteIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_delete)!!

                val moreOptionsEnabled = a.getBoolean(
                    R.styleable.ChannelListView_streamUiChannelOptionsEnabled,
                    true
                )

                val deleteEnabled = a.getBoolean(
                    R.styleable.ChannelListView_streamUiChannelDeleteEnabled,
                    true
                )

                val swipeEnabled = a.getBoolean(
                    R.styleable.ChannelListView_streamUiSwipeEnabled,
                    true
                )

                val backgroundLayoutColor = a.getColor(
                    R.styleable.ChannelListView_streamUiBackgroundLayoutColor,
                    context.getColorCompat(R.color.stream_ui_white_smoke)
                )

                val channelTitleText = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelListView_streamUiChannelTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_title)
                    )
                    .color(
                        R.styleable.ChannelListView_streamUiChannelTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.ChannelListView_streamUiChannelTitleFontAssets,
                        R.styleable.ChannelListView_streamUiChannelTitleTextFont
                    )
                    .style(
                        R.styleable.ChannelListView_streamUiChannelTitleTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val lastMessageText = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelListView_streamUiLastMessageTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_message)
                    )
                    .color(
                        R.styleable.ChannelListView_streamUiLastMessageTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.ChannelListView_streamUiLastMessageFontAssets,
                        R.styleable.ChannelListView_streamUiLastMessageTextFont
                    )
                    .style(
                        R.styleable.ChannelListView_streamUiLastMessageTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val lastMessageDateText = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelListView_streamUiLastMessageDateTextSize,
                        context.getDimension(R.dimen.stream_ui_channel_item_message_date)
                    )
                    .color(
                        R.styleable.ChannelListView_streamUiLastMessageDateTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.ChannelListView_streamUiLastMessageDateFontAssets,
                        R.styleable.ChannelListView_streamUiLastMessageDateTextFont
                    )
                    .style(
                        R.styleable.ChannelListView_streamUiLastMessageDateTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val indicatorSentIcon = a.getDrawable(R.styleable.ChannelListView_streamUiIndicatorSentIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_check_single)!!

                val indicatorReadIcon = a.getDrawable(R.styleable.ChannelListView_streamUiIndicatorReadIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_check_double)!!

                val indicatorPendingSyncIcon =
                    a.getDrawable(R.styleable.ChannelListView_streamUiIndicatorPendingSyncIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clock)!!

                val foregroundLayoutColor = a.getColor(
                    R.styleable.ChannelListView_streamUiForegroundLayoutColor,
                    context.getColorCompat(R.color.stream_ui_white_snow)
                )

                val unreadMessageCounterText = TextStyle.Builder(a)
                    .size(
                        R.styleable.ChannelListView_streamUiUnreadMessageCounterTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.ChannelListView_streamUiUnreadMessageCounterTextColor,
                        context.getColorCompat(R.color.stream_ui_literal_white)
                    )
                    .font(
                        R.styleable.ChannelListView_streamUiUnreadMessageCounterFontAssets,
                        R.styleable.ChannelListView_streamUiUnreadMessageCounterTextFont
                    )
                    .style(
                        R.styleable.ChannelListView_streamUiUnreadMessageCounterTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val unreadMessageCounterBackgroundColor = a.getColor(
                    R.styleable.ChannelListView_streamUiUnreadMessageCounterBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_accent_red)
                )

                return ChannelListViewStyle(
                    optionsIcon = optionsIcon,
                    deleteIcon = deleteIcon,
                    optionsEnabled = moreOptionsEnabled,
                    deleteEnabled = deleteEnabled,
                    swipeEnabled = swipeEnabled,
                    backgroundLayoutColor = backgroundLayoutColor,
                    channelTitleText = channelTitleText,
                    lastMessageText = lastMessageText,
                    lastMessageDateText = lastMessageDateText,
                    indicatorSentIcon = indicatorSentIcon,
                    indicatorReadIcon = indicatorReadIcon,
                    indicatorPendingSyncIcon = indicatorPendingSyncIcon,
                    foregroundLayoutColor = foregroundLayoutColor,
                    unreadMessageCounterText = unreadMessageCounterText,
                    unreadMessageCounterBackgroundColor = unreadMessageCounterBackgroundColor
                ).let(TransformStyle.channelListStyleTransformer::transform)
            }
        }
    }
}
