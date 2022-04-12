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

package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItemViewType
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal.ChannelListLoadingMoreViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal.ChannelViewHolder

public open class ChannelListItemViewHolderFactory {

    protected lateinit var listenerContainer: ChannelListListenerContainer
        private set

    protected lateinit var style: ChannelListViewStyle
        private set

    internal fun setListenerContainer(listenerContainer: ChannelListListenerContainer) {
        this.listenerContainer = listenerContainer
    }

    internal fun setStyle(style: ChannelListViewStyle) {
        this.style = style
    }

    /**
     * Returns a view type value based on the type and contents of the given [item].
     * The view type returned here will be used as a parameter in [createViewHolder].
     *
     * For built-in view types, see [ChannelListItemType] and its constants.
     */
    public open fun getItemViewType(item: ChannelListItem): Int {
        return when (item) {
            is ChannelListItem.LoadingMoreItem -> ChannelListItemViewType.LOADING_MORE
            is ChannelListItem.ChannelItem -> ChannelListItemViewType.DEFAULT
        }
    }

    /**
     * Creates a new ViewHolder to be used in the Message List.
     * The [viewType] parameter is determined by [getItemViewType].
     */
    public open fun createViewHolder(
        parentView: ViewGroup,
        viewType: Int,
    ): BaseChannelListItemViewHolder {
        return when (viewType) {
            ChannelListItemViewType.DEFAULT -> createChannelViewHolder(parentView)
            ChannelListItemViewType.LOADING_MORE -> createLoadingMoreViewHolder(parentView)
            else -> throw IllegalArgumentException("Unhandled ChannelList view type: $viewType")
        }
    }

    protected open fun createChannelViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
        return ChannelViewHolder(
            parentView,
            listenerContainer.channelClickListener,
            listenerContainer.channelLongClickListener,
            listenerContainer.deleteClickListener,
            listenerContainer.moreOptionsClickListener,
            listenerContainer.userClickListener,
            listenerContainer.swipeListener,
            style,
        )
    }

    protected open fun createLoadingMoreViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
        return ChannelListLoadingMoreViewHolder(parentView, style)
    }
}
