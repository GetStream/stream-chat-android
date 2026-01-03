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

package io.getstream.chat.android.ui.feature.threads.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.ui.feature.threads.list.ThreadListView
import io.getstream.chat.android.ui.feature.threads.list.ThreadListViewStyle
import io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder.BaseThreadListItemViewHolder
import io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder.internal.ThreadItemViewHolder
import io.getstream.chat.android.ui.feature.threads.list.adapter.viewholder.internal.ThreadListLoadingMoreViewHolder

/**
 * Factory responsible for creating ViewHolder instances for the RecyclerView used in the
 * [io.getstream.chat.android.ui.feature.threads.list.ThreadListView].
 */
public open class ThreadListItemViewHolderFactory {

    /**
     * The [ThreadListViewStyle] for styling the viewHolders.
     */
    protected lateinit var style: ThreadListViewStyle
        private set

    /**
     * The listener for clicks on the thread items.
     */
    protected var clickListener: ThreadListView.ThreadClickListener? = null
        private set

    /**
     * Returns a view type value based on the type of the given [item].
     * The view type returned here will be used as a parameter in [createViewHolder].
     *
     * For built-in view types, see [ThreadListItemViewType] and its constants.
     *
     * @param item The [ThreadListItem] to check the type of.
     */
    public open fun getItemViewType(item: ThreadListItem): Int {
        return when (item) {
            is ThreadListItem.ThreadItem -> ThreadListItemViewType.ITEM_THREAD
            is ThreadListItem.LoadingMoreItem -> ThreadListItemViewType.ITEM_LOADING_MORE
        }
    }

    /**
     * Returns a view type value based on the type of the given [viewHolder].
     *
     * For built-in view types, see [ThreadListItemViewType] and its constants.
     *
     * @param viewHolder The [BaseThreadListItemViewHolder] to check the type of.
     */
    public open fun getItemViewType(viewHolder: BaseThreadListItemViewHolder<out ThreadListItem>): Int {
        return when (viewHolder) {
            is ThreadItemViewHolder -> ThreadListItemViewType.ITEM_THREAD
            is ThreadListLoadingMoreViewHolder -> ThreadListItemViewType.ITEM_LOADING_MORE
            else -> throw IllegalArgumentException("Unhandled ThreadList view holder: $viewHolder")
        }
    }

    /**
     * Creates a new ViewHolder based on the provided [viewType] to be used in the Thread List.
     * The [viewType] parameter is determined by [getItemViewType].
     *
     * @param parentView The parent of the view.
     * @param viewType The type of the item for which the viewHolder is created.
     */
    public open fun createViewHolder(
        parentView: ViewGroup,
        viewType: Int,
    ): BaseThreadListItemViewHolder<out ThreadListItem> {
        return when (viewType) {
            ThreadListItemViewType.ITEM_THREAD -> createThreadItemViewHolder(parentView)
            ThreadListItemViewType.ITEM_LOADING_MORE -> createLoadingMoreViewHolder(parentView)
            else -> throw IllegalArgumentException("Unhandled ThreadList view type: $viewType")
        }
    }

    /**
     * Creates the ViewHolder for the [ThreadListItemViewType.ITEM_THREAD] ([ThreadListItem.ThreadItem]) type.
     *
     * @param parentView The parent of the view.
     */
    protected open fun createThreadItemViewHolder(
        parentView: ViewGroup,
    ): BaseThreadListItemViewHolder<ThreadListItem.ThreadItem> {
        return ThreadItemViewHolder(parentView, style, clickListener)
    }

    /**
     * Creates the ViewHolder for the [ThreadListItemViewType.ITEM_LOADING_MORE] ([ThreadListItem.LoadingMoreItem])
     * type.
     *
     * @param parentView The parent of the view.
     */
    protected open fun createLoadingMoreViewHolder(
        parentView: ViewGroup,
    ): BaseThreadListItemViewHolder<ThreadListItem.LoadingMoreItem> {
        return ThreadListLoadingMoreViewHolder(parentView)
    }

    /**
     * Sets the [ThreadListViewStyle] to be used by the created ViewHolders.
     */
    internal fun setStyle(style: ThreadListViewStyle) {
        this.style = style
    }

    /**
     * Sets the [ThreadListView.ThreadClickListener] for clicks on the thread items.
     */
    internal fun setThreadClickListener(clickListener: ThreadListView.ThreadClickListener?) {
        this.clickListener = clickListener
    }
}
