/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.channels.list

import android.content.Context
import androidx.annotation.Px
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.channels.ChannelListFragment
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getDimension

/**
 * Style for [ChannelListFragment].
 * Use this class together with [TransformStyle.channelListFragmentStyleTransformer] to change [ChannelListFragment] styles programmatically.
 *
 * @property searchInputMarginStart The start margin of the search input.
 * @property searchInputMarginTop The top margin of the search input.
 * @property searchInputMarginEnd The end margin of the search input.
 * @property searchInputMarginBottom The bottom margin of the search input.
 */
public data class ChannelListFragmentViewStyle(
    @Px public val searchInputMarginStart: Int,
    @Px public val searchInputMarginTop: Int,
    @Px public val searchInputMarginEnd: Int,
    @Px public val searchInputMarginBottom: Int,
) : ViewStyle {

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
