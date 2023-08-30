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
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.ViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal.ChannelViewHolder
import io.getstream.chat.android.ui.common.extensions.internal.getDimension

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
 * @property itemHeight Height of the channel list item. Default value is [R.dimen.stream_ui_channel_list_item_height].
 * @property itemMarginStart Start margin of the channel list item. Default value is [R.dimen.stream_ui_channel_list_item_margin_start].
 * @property itemMarginEnd End margin of the channel list item. Default value is [R.dimen.stream_ui_channel_list_item_margin_end].
 * @property itemTitleMarginStart Start margin of the channel list item title. Default value is [R.dimen.stream_ui_channel_list_item_title_margin_start].
 * @property itemVerticalSpacerHeight Height of the channel list item vertical spacer. Default value is [R.dimen.stream_ui_channel_list_item_vertical_spacer_height].
 * @property itemVerticalSpacerPosition Position of the channel list item vertical spacer. Default value is [R.dimen.stream_ui_channel_list_item_vertical_spacer_position].
 */
public data class ChannelListFragmentViewStyle(
    @Px public val searchInputMarginStart: Int,
    @Px public val searchInputMarginTop: Int,
    @Px public val searchInputMarginEnd: Int,
    @Px public val searchInputMarginBottom: Int,
): ViewStyle {

    internal companion object {
        operator fun invoke(context: Context): ChannelListFragmentViewStyle {
            val searchInputMarginTop: Int = context.getDimension(R.dimen.stream_ui_channel_list_search_margin_top)
            val searchInputMarginStart: Int = context.getDimension(R.dimen.stream_ui_channel_list_search_margin_start)
            val searchInputMarginEnd: Int = context.getDimension(R.dimen.stream_ui_channel_list_search_margin_end)
            val searchInputMarginBottom: Int = context.getDimension(R.dimen.stream_ui_channel_list_search_margin_bottom)

            return ChannelListFragmentViewStyle(
                searchInputMarginStart = searchInputMarginStart,
                searchInputMarginTop = searchInputMarginTop,
                searchInputMarginEnd = searchInputMarginEnd,
                searchInputMarginBottom = searchInputMarginBottom,
            ).let(TransformStyle.channelListFragmentStyleTransformer::transform)
        }
    }
}
