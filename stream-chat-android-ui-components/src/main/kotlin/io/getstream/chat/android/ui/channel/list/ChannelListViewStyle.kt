package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal.ChannelViewHolder
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 * Style for [ChannelListView].
 * Use this class together with [TransformStyle.channelListStyleTransformer] to change [ChannelListView] styles programmatically.
 *
 * @property optionsIcon - icon for channel's options. Default - [R.drawable.stream_ui_ic_more]
 * @property deleteIcon - icon for deleting channel option. Default - [R.drawable.stream_ui_ic_delete]
 * @property optionsEnabled - enables/disables channel's options. Enabled by default
 * @property deleteEnabled - enables/disables delete channel option. Enabled by default
 * @property swipeEnabled - enables/disables swipe on channel list item. Enabled by default
 * @property backgroundLayoutColor - background color for [ChannelViewHolder]. Default - [R.color.stream_ui_white_smoke]
 * @property channelTitleText - appearance for channel's title, displayed in [ChannelViewHolder]
 * @property lastMessageText - appearance for last message text, displayed in [ChannelViewHolder]
 * @property lastMessageDateText - appearance for last message date text displayed in [ChannelViewHolder]
 * @property indicatorSentIcon - icon for indicating message sent status in [ChannelViewHolder]. Default - [R.drawable.stream_ui_ic_check_single]
 * @property indicatorReadIcon - icon for indicating message read status in [ChannelViewHolder]. Default - [R.drawable.stream_ui_ic_check_double]
 * @property indicatorPendingSyncIcon - icon for indicating sync pending status in [ChannelViewHolder]. Default - [R.drawable.stream_ui_ic_clock]
 * @property foregroundLayoutColor - foreground color for [ChannelViewHolder]. Default - [R.color.stream_ui_white_snow]
 * @property unreadMessageCounterText - appearance for message counter text, displayed in [ChannelViewHolder]
 * @property unreadMessageCounterBackgroundColor - background color for message counter, displayed in [ChannelViewHolder]. Default - [R.color.stream_ui_accent_red]
 * @property mutedChannelIcon - icon for muted channel, displayed in [ChannelViewHolder]. Default - [R.drawable.stream_ui_ic_mute_black]
 * @property mutedChannelIconTint - tint for mutedChannelIcon. Default - [R.color.stream_ui_black]
 */
public data class ChannelListViewStyle(
    public val optionsIcon: Drawable,
    public val deleteIcon: Drawable,
    public val optionsEnabled: Boolean,
    public val deleteEnabled: Boolean,
    public val swipeEnabled: Boolean,
    @ColorInt public val backgroundLayoutColor: Int,
    public val channelTitleText: TextStyle,
    public val lastMessageText: TextStyle,
    public val lastMessageDateText: TextStyle,
    public val indicatorSentIcon: Drawable,
    public val indicatorReadIcon: Drawable,
    public val indicatorPendingSyncIcon: Drawable,
    @ColorInt public val foregroundLayoutColor: Int,
    public val unreadMessageCounterText: TextStyle,
    @ColorInt public val unreadMessageCounterBackgroundColor: Int,
    public val mutedChannelIcon: Drawable,
    @Deprecated(message = "Use mutedChannelIcon instead", level = DeprecationLevel.ERROR)
    @ColorInt public val mutedChannelIconTint: Int?,
    public val itemSeparator: Drawable,
    @LayoutRes public val loadingView: Int,
    @LayoutRes public val emptyStateView: Int,
    @LayoutRes public val loadingMoreView: Int,
) {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): ChannelListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ChannelListView,
                R.attr.streamUiChannelListViewStyle,
                R.style.StreamUi_ChannelListView,
            ).use { a ->
                val optionsIcon = a.getDrawable(R.styleable.ChannelListView_streamUiChannelOptionsIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_more)!!

                val deleteIcon = a.getDrawable(R.styleable.ChannelListView_streamUiChannelDeleteIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_delete)!!

                val moreOptionsEnabled = a.getBoolean(
                    R.styleable.ChannelListView_streamUiChannelOptionsEnabled,
                    false
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

                val mutedChannelIcon = a.getDrawable(
                    R.styleable.ChannelListView_streamUiMutedChannelIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mute_black)!!

                val mutedChannelIconTint = a.getColorOrNull(R.styleable.ChannelListView_streamUiMutedChannelIconTint)?.also { tint ->
                    mutedChannelIcon.setTint(tint)
                }

                val itemSeparator = a.getDrawable(
                    R.styleable.ChannelListView_streamUiChannelsItemSeparatorDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_divider)!!

                val loadingView = a.getResourceId(
                    R.styleable.ChannelListView_streamUiLoadingView,
                    R.layout.stream_ui_channel_list_loading_view,
                )

                val emptyStateView = a.getResourceId(
                    R.styleable.ChannelListView_streamUiEmptyStateView,
                    R.layout.stream_ui_channel_list_empty_state_view,
                )

                val loadingMoreView = a.getResourceId(
                    R.styleable.ChannelListView_streamUiLoadingMoreView,
                    R.layout.stream_ui_channel_list_loading_more_view,
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
                    unreadMessageCounterBackgroundColor = unreadMessageCounterBackgroundColor,
                    mutedChannelIcon = mutedChannelIcon,
                    mutedChannelIconTint = mutedChannelIconTint,
                    itemSeparator = itemSeparator,
                    loadingView = loadingView,
                    emptyStateView = emptyStateView,
                    loadingMoreView = loadingMoreView,
                ).let(TransformStyle.channelListStyleTransformer::transform)
            }
        }
    }
}
