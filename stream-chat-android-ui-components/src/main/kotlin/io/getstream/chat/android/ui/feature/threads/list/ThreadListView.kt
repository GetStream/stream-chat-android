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

package io.getstream.chat.android.ui.feature.threads.list

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiThreadListViewBinding
import io.getstream.chat.android.ui.feature.threads.list.adapter.ThreadListItem
import io.getstream.chat.android.ui.feature.threads.list.adapter.ThreadListItemViewHolderFactory
import io.getstream.chat.android.ui.feature.threads.list.adapter.internal.ThreadListAdapter
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.EndlessScrollListener

/**
 * View rendering a paginated list of threads.
 * Optionally, it renders a banner informing about new threads/thread messages outside of the loaded pages of threads.
 */
public class ThreadListView : ConstraintLayout {

    private val binding = StreamUiThreadListViewBinding.inflate(streamThemeInflater, this)
    private lateinit var style: ThreadListViewStyle
    private lateinit var viewHolderFactory: ThreadListItemViewHolderFactory
    private lateinit var adapter: ThreadListAdapter
    private var clickListener: ThreadClickListener? = null
    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.onLoadMore()
    }

    /**
     * Creates a [ThreadListView] from the given [Context].
     */
    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    /**
     * Creates a [ThreadListView] from the given [Context] and [AttributeSet].
     */
    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    private var loadMoreListener: LoadMoreListener? = null

    private fun init(attrs: AttributeSet?) {
        style = ThreadListViewStyle(context, attrs)

        binding.threadListRecyclerView.addOnScrollListener(scrollListener)

        setBackgroundColor(style.backgroundColor)
        applyEmptyStateStyle(style)
        applyBannerStyle(style)
    }

    /**
     * Shows a list of threads.
     *
     * @param threads The list of [Thread]s to show.
     * @param isLoadingMore Indicator if the loading more view should be shown.
     */
    public fun showThreads(threads: List<Thread>, isLoadingMore: Boolean) {
        val isCurrentlyEmpty = requireAdapter().itemCount == 0
        val hasThreads = threads.isNotEmpty()

        binding.threadListRecyclerView.isVisible = hasThreads
        binding.emptyContainer.isVisible = !hasThreads
        binding.progressBar.isVisible = false

        val threadItems = threads.map(ThreadListItem::ThreadItem)
        val loadingMoreItems = if (isLoadingMore) listOf(ThreadListItem.LoadingMoreItem) else emptyList()
        requireAdapter().submitList(threadItems + loadingMoreItems)

        scrollListener.enablePagination()

        if (isCurrentlyEmpty && hasThreads) {
            // Data is fully reloaded, ensure list is scrolled to the top
            binding.threadListRecyclerView.scrollToPosition(0)
        }
    }

    /**
     * Shows the loading state of the thread list.
     */
    public fun showLoading() {
        requireAdapter().submitList(emptyList()) // clear current list
        binding.threadListRecyclerView.isVisible = false
        binding.emptyContainer.isVisible = false
        binding.progressBar.isVisible = true
        scrollListener.disablePagination()
    }

    /**
     * Show the 'unread threads' banner.
     * Hides the banner if [unreadThreadsCount] == 0.
     *
     * @param unreadThreadsCount The number of unread threads.
     */
    public fun showUnreadThreadsBanner(unreadThreadsCount: Int) {
        val bannerText = context.resources.getQuantityString(
            R.plurals.stream_ui_thread_list_new_threads,
            unreadThreadsCount,
            unreadThreadsCount,
        )
        binding.unreadThreadsBannerTextView.isVisible = unreadThreadsCount > 0
        binding.unreadThreadsBannerTextView.text = bannerText
    }

    /**
     * Sets the [ThreadListItemViewHolderFactory] used to create the thread list view holders.
     * Use if you want completely custom views for the thread list items.
     * Make sure to call this before setting/updating the data in the thread list view.
     *
     * @param factory The [ThreadListItemViewHolderFactory] to be used for creating the item view holders.
     * @throws IllegalStateException if called when a [factory] was already set.
     */
    public fun setViewHolderFactory(factory: ThreadListItemViewHolderFactory) {
        check(::adapter.isInitialized.not()) {
            "Adapter was already initialized, please set ChannelListItemViewHolderFactory first"
        }
        viewHolderFactory = factory
    }

    /**
     * Sets the listener for clicks on the unread threads banner.
     *
     * @param listener The [UnreadThreadsBannerClickListener] to be invoked when the user clicks on the unread threads
     * banner.
     */
    public fun setUnreadThreadsBannerClickListener(listener: UnreadThreadsBannerClickListener) {
        binding.unreadThreadsBannerTextView.setOnClickListener {
            listener.onUnreadThreadsBannerClick()
        }
    }

    /**
     * Sets the listener for clicks on threads.
     *
     * @param listener The [ThreadClickListener] to be invoked when the user clicks on a thread.
     */
    public fun setThreadClickListener(listener: ThreadClickListener) {
        this.clickListener = listener
    }

    /**
     * Sets the listener requesting loading of more threads.
     *
     * @param listener The [LoadMoreListener] to be invoked when the end of the thread list is reached.
     */
    public fun setLoadMoreListener(listener: LoadMoreListener) {
        this.loadMoreListener = listener
    }

    /**
     * Ensures the [adapter] is initialized before accessing it.
     * Useful for cases where a custom [viewHolderFactory] is provided.
     */
    private fun requireAdapter(): ThreadListAdapter {
        if (::adapter.isInitialized.not()) {
            initAdapter()
        }
        return adapter
    }

    private fun initAdapter() {
        // Ensure the viewHolderFactory is initialized
        if (::viewHolderFactory.isInitialized.not()) {
            viewHolderFactory = ThreadListItemViewHolderFactory()
        }
        viewHolderFactory.setStyle(style)
        viewHolderFactory.setThreadClickListener(clickListener)
        adapter = ThreadListAdapter(style, viewHolderFactory)
        binding.threadListRecyclerView.adapter = adapter
    }

    private fun applyEmptyStateStyle(style: ThreadListViewStyle) {
        binding.emptyImage.setImageDrawable(style.emptyStateDrawable)
        binding.emptyTextView.text = style.emptyStateText
        binding.emptyTextView.setTextStyle(style.emptyStateTextStyle)
    }

    private fun applyBannerStyle(style: ThreadListViewStyle) {
        binding.unreadThreadsBannerTextView.setTextStyle(style.bannerTextStyle)
        binding.unreadThreadsBannerTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            style.bannerIcon,
            null,
        )
        binding.unreadThreadsBannerTextView.background = style.bannerBackground
        binding.unreadThreadsBannerTextView.updatePadding(
            left = style.bannerPaddingLeft,
            top = style.bannerPaddingTop,
            right = style.bannerPaddingRight,
            bottom = style.bannerPaddingBottom,
        )
        binding.unreadThreadsBannerTextView.updateLayoutParams<MarginLayoutParams> {
            leftMargin = style.bannerMarginLeft
            topMargin = style.bannerMarginTop
            rightMargin = style.bannerMarginRight
            bottomMargin = style.bannerMarginBottom
        }
    }

    private companion object {
        private const val LOAD_MORE_THRESHOLD = 10
    }

    /**
     * Listener for clicks on the "unread threads" banner.
     */
    public fun interface UnreadThreadsBannerClickListener {

        /**
         * Called when the user clicks on the unread threads banner.
         */
        public fun onUnreadThreadsBannerClick()
    }

    /**
     * Listener for clicks on the thread list.
     */
    public fun interface ThreadClickListener {

        /**
         * Called when the user clicks on a thread in the list.
         *
         * @param thread The clicked [Thread].
         */
        public fun onThreadClick(thread: Thread)
    }

    /**
     * Listener invoked when the end of the thread list is reached, and a new page of threads should be loaded.
     */
    public fun interface LoadMoreListener {

        /**
         * Called when a new page of threads should be loaded.
         */
        public fun onLoadMore()
    }
}
