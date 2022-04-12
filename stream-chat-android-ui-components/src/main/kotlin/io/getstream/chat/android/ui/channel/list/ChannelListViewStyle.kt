/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @property optionsIcon Icon for channel's options. Default value is [R.drawable.stream_ui_ic_more].
 * @property deleteIcon Icon for deleting channel option. Default value is [R.drawable.stream_ui_ic_delete].
 * @property optionsEnabled Enables/disables channel's options. Enabled by default
 * @property deleteEnabled Enables/disables delete channel option. Enabled by default
 * @property swipeEnabled Enables/disables swipe on channel list item. Enabled by default
 * @property backgroundLayoutColor Background color for [ChannelViewHolder]. Default value is [R.color.stream_ui_white_smoke].
 * @property channelTitleText Appearance for channel's title, displayed in [ChannelViewHolder]
 * @property lastMessageText Appearance for last message text, displayed in [ChannelViewHolder]
 * @property lastMessageDateText Appearance for last message date text displayed in [ChannelViewHolder]
 * @property indicatorSentIcon Icon for indicating message sent status in [ChannelViewHolder]. Default value is [R.drawable.stream_ui_ic_check_single].
 * @property indicatorReadIcon Icon for indicating message read status in [ChannelViewHolder]. Default value is [R.drawable.stream_ui_ic_check_double].
 * @property indicatorPendingSyncIcon Icon for indicating sync pending status in [ChannelViewHolder]. Default value is [R.drawable.stream_ui_ic_clock].
 * @property foregroundLayoutColor Foreground color for [ChannelViewHolder]. Default value is [R.color.stream_ui_white_snow].
 * @property unreadMessageCounterText Appearance for message counter text, displayed in [ChannelViewHolder]
 * @property unreadMessageCounterBackgroundColor Background color for message counter, displayed in [ChannelViewHolder]. Default value is [R.color.stream_ui_accent_red].
 * @property mutedChannelIcon Icon for muted channel, displayed in [ChannelViewHolder]. Default value is [R.drawable.stream_ui_ic_mute_black].
 * @property itemSeparator Items' separator. Default value is [R.drawable.stream_ui_divider].
 * @property loadingView Loading view. Default value is [R.layout.stream_ui_default_loading_view].
 * @property emptyStateView Empty state view. Default value is [R.layout.stream_ui_channel_list_empty_state_view].
 * @property loadingMoreView Loading more view. Default value is [R.layout.stream_ui_channel_list_loading_more_view].
 * @property edgeEffectColor Color applied to the [ChannelListView] edge effect. Pass null if you want to use default [android.R.attr.colorEdgeEffect]. Default value is null.
 * @property showChannelDeliveryStatusIndicator Flag if we need to show the delivery indicator or not.
 */
public data class ChannelListViewStyle(
    public val optionsIcon: Drawable,
    public val deleteIcon: Drawable,
    public val optionsEnabled: Boolean,
    public val deleteEnabled: Boolean,
    public val swipeEnabled: Boolean,
    @ColorInt public val backgroundColor: Int,
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
    public val itemSeparator: Drawable,
    @LayoutRes public val loadingView: Int,
    @LayoutRes public val emptyStateView: Int,
    @LayoutRes public val loadingMoreView: Int,
    @ColorInt public val edgeEffectColor: Int?,
    public val showChannelDeliveryStatusIndicator: Boolean
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

                val backgroundColor = a.getColor(
                    R.styleable.ChannelListView_streamUiChannelListBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white)
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

                val showChannelDeliveryStatusIndicator = a.getBoolean(
                    R.styleable.ChannelListView_streamUiShowChannelDeliveryStatusIndicator,
                    true
                )

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

                val itemSeparator = a.getDrawable(
                    R.styleable.ChannelListView_streamUiChannelsItemSeparatorDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_divider)!!

                val loadingView = a.getResourceId(
                    R.styleable.ChannelListView_streamUiLoadingView,
                    R.layout.stream_ui_default_loading_view,
                )

                val emptyStateView = a.getResourceId(
                    R.styleable.ChannelListView_streamUiEmptyStateView,
                    R.layout.stream_ui_channel_list_empty_state_view,
                )

                val loadingMoreView = a.getResourceId(
                    R.styleable.ChannelListView_streamUiLoadingMoreView,
                    R.layout.stream_ui_channel_list_loading_more_view,
                )

                val edgeEffectColor = a.getColorOrNull(R.styleable.ChannelListView_streamUiEdgeEffectColor)

                return ChannelListViewStyle(
                    optionsIcon = optionsIcon,
                    deleteIcon = deleteIcon,
                    optionsEnabled = moreOptionsEnabled,
                    deleteEnabled = deleteEnabled,
                    swipeEnabled = swipeEnabled,
                    backgroundColor = backgroundColor,
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
                    itemSeparator = itemSeparator,
                    loadingView = loadingView,
                    emptyStateView = emptyStateView,
                    loadingMoreView = loadingMoreView,
                    edgeEffectColor = edgeEffectColor,
                    showChannelDeliveryStatusIndicator = showChannelDeliveryStatusIndicator
                ).let(TransformStyle.channelListStyleTransformer::transform)
            }
        }
    }
}
