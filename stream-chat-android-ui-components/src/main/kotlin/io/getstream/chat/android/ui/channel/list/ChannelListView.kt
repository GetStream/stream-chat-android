package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelClickListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelLongClickListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.UserClickListener
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItemAdapter
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelItemSwipeListener
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListListenerContainerImpl
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder
import io.getstream.chat.android.ui.utils.extensions.cast

public class ChannelListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle) {

    private val layoutManager: ScrollPauseLinearLayoutManager
    private val scrollListener: EndReachedScrollListener = EndReachedScrollListener()
    private val dividerDecoration: SimpleVerticalListDivider = SimpleVerticalListDivider()

    private var endReachedListener: EndReachedListener? = null

    private lateinit var viewHolderFactory: ChannelListItemViewHolderFactory

    private lateinit var adapter: ChannelListItemAdapter

    internal val listenerContainer = ChannelListListenerContainerImpl()

    private val style: ChannelListViewStyle

    init {
        setHasFixedSize(true)
        layoutManager = ScrollPauseLinearLayoutManager(context)
        setLayoutManager(layoutManager)
        setSwipeListener(ChannelItemSwipeListener(this, layoutManager))

        style = ChannelListViewStyle(context, attrs)

        addItemDecoration(dividerDecoration)
    }

    private fun requireAdapter(): ChannelListItemAdapter {
        if (::adapter.isInitialized.not()) {
            initAdapter()
        }
        return adapter
    }

    private fun initAdapter() {
        // Create default ViewHolderFactory if needed
        if (::viewHolderFactory.isInitialized.not()) {
            viewHolderFactory = ChannelListItemViewHolderFactory()
        }

        viewHolderFactory.setListenerContainer(this.listenerContainer)
        viewHolderFactory.setStyle(style)

        adapter = ChannelListItemAdapter(viewHolderFactory)

        this.setAdapter(adapter)
    }

    public fun setViewHolderFactory(viewHolderFactory: ChannelListItemViewHolderFactory) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set ChannelListItemViewHolderFactory first" }

        this.viewHolderFactory = viewHolderFactory
    }

    public fun setChannelClickListener(listener: ChannelClickListener?) {
        listenerContainer.channelClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setChannelLongClickListener(listener: ChannelLongClickListener?) {
        listenerContainer.channelLongClickListener = listener ?: ChannelLongClickListener.DEFAULT
    }

    public fun setUserClickListener(listener: UserClickListener?) {
        listenerContainer.userClickListener = listener ?: UserClickListener.DEFAULT
    }

    public fun setChannelDeleteClickListener(listener: ChannelClickListener?) {
        listenerContainer.deleteClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setMoreOptionsClickListener(listener: ChannelClickListener?) {
        listenerContainer.moreOptionsClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setSwipeListener(listener: SwipeListener?) {
        listenerContainer.swipeListener = listener ?: SwipeListener.DEFAULT
    }

    public fun setItemSeparator(@DrawableRes drawableResource: Int) {
        dividerDecoration.drawableResource = drawableResource
    }

    public fun setItemSeparatorHeight(height: Int) {
        dividerDecoration.drawableHeight = height
    }

    public fun setShouldDrawItemSeparatorOnLastItem(shouldDrawOnLastItem: Boolean) {
        dividerDecoration.drawOnLastItem = shouldDrawOnLastItem
    }

    public fun setOnEndReachedListener(listener: EndReachedListener?) {
        endReachedListener = listener
        observeListEndRegion()
    }

    private fun observeListEndRegion() {
        addOnScrollListener(scrollListener)
    }

    public fun setPaginationEnabled(enabled: Boolean) {
        scrollListener.setPaginationEnabled(enabled)
    }

    public fun setChannels(channels: List<ChannelListItem>) {
        requireAdapter().submitList(channels)
    }

    public fun showLoadingMore(show: Boolean) {
        requireAdapter().let { adapter ->
            val currentList = adapter.currentList
            val loadingMore = currentList.contains(ChannelListItem.LoadingMoreItem)
            val showLoadingMore = show && !loadingMore
            val hideLoadingMore = !show && loadingMore

            val updatedList = when {
                showLoadingMore -> currentList + ChannelListItem.LoadingMoreItem

                // we should never have more than one loading item, but just in case
                hideLoadingMore -> currentList.filterIsInstance(ChannelListItem.ChannelItem::class.java)

                else -> currentList
            }

            adapter.submitList(updatedList) {
                if (showLoadingMore) {
                    layoutManager.scrollToPosition(updatedList.size - 1)
                }
            }
        }
    }

    public fun hasChannels(): Boolean {
        return requireAdapter().itemCount > 0
    }

    internal fun getChannel(cid: String): Channel = adapter.getChannel(cid)

    public override fun onVisibilityChanged(view: View, visibility: Int) {
        super.onVisibilityChanged(view, visibility)
        if (visibility == View.VISIBLE && ::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    public fun interface UserClickListener {
        public companion object {
            @JvmField
            public val DEFAULT: UserClickListener = UserClickListener {}
        }

        public fun onClick(user: User)
    }

    public fun interface ChannelClickListener {
        public companion object {
            @JvmField
            public val DEFAULT: ChannelClickListener = ChannelClickListener {}
        }

        public fun onClick(channel: Channel)
    }

    public fun interface ChannelLongClickListener {
        public companion object {
            @JvmField
            public val DEFAULT: ChannelLongClickListener = ChannelLongClickListener {
                // consume the long click by default so that it doesn't become a regular click
                true
            }
        }

        /**
         * Called when a channel has been clicked and held.
         *
         * @return true if the callback consumed the long click, false otherwise.
         */
        public fun onLongClick(channel: Channel): Boolean
    }

    public fun interface EndReachedListener {
        public fun onEndReached()
    }

    private inner class EndReachedScrollListener : OnScrollListener() {
        private var enabled = false
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (SCROLL_STATE_IDLE == newState) {
                val linearLayoutManager = getLayoutManager()?.cast<LinearLayoutManager>()
                val lastVisiblePosition = linearLayoutManager?.findLastVisibleItemPosition()
                val reachedTheEnd = requireAdapter().itemCount - 1 == lastVisiblePosition
                if (reachedTheEnd && enabled) {
                    endReachedListener?.onEndReached()
                }
            }
        }

        fun setPaginationEnabled(enabled: Boolean) {
            this.enabled = enabled
        }
    }

    public interface SwipeListener {
        /**
         * Invoked when a swipe is detected.
         *
         * @param viewHolder the view holder that is being swiped
         * @param adapterPosition the internal adapter position of the item being bound
         * @param x the raw X of the swipe origin; null may indicate the call isn't from user interaction
         * @param y the raw Y of the swipe origin; null may indicate the call isn't from user interaction
         */
        public fun onSwipeStarted(viewHolder: SwipeViewHolder, adapterPosition: Int, x: Float? = null, y: Float? = null)

        /**
         * Invoked after a swipe has been detected, and movement is occurring.
         *
         * @param viewHolder the view holder that is being swiped
         * @param adapterPosition the internal adapter position of the item being bound
         * @param dX the change from the previous swipe touch event to the current
         * @param totalDeltaX the change from the first touch event to the current
         */
        public fun onSwipeChanged(viewHolder: SwipeViewHolder, adapterPosition: Int, dX: Float, totalDeltaX: Float)

        /**
         * Invoked when a swipe is successfully completed naturally, without cancellation.
         *
         * @param viewHolder the view holder that is being swiped
         * @param adapterPosition the internal adapter position of the item being bound
         * @param x the raw X of the swipe origin; null may indicate the call isn't from user interaction
         * @param y the raw Y of the swipe origin; null may indicate the call isn't from user interaction
         */
        public fun onSwipeCompleted(
            viewHolder: SwipeViewHolder,
            adapterPosition: Int,
            x: Float? = null,
            y: Float? = null,
        )

        /**
         * Invoked when a swipe is canceled.
         *
         * @param viewHolder the view holder that is being swiped
         * @param adapterPosition the internal adapter position of the item being bound
         * @param x the raw X of the swipe origin; null may indicate the call isn't from user interaction
         * @param y the raw Y of the swipe origin; null may indicate the call isn't from user interaction         */
        public fun onSwipeCanceled(
            viewHolder: SwipeViewHolder,
            adapterPosition: Int,
            x: Float? = null,
            y: Float? = null,
        )

        /**
         * Invoked in order to set the [viewHolder]'s initial state when bound. This supports view holder reuse.
         * When items are scrolled off-screen and the view holder is reused, it becomes important to
         * track the swiped state and determine if the view holder should appear as swiped for the item
         * being bound.
         *
         * @param viewHolder the view holder being bound
         * @param adapterPosition the internal adapter position of the item being bound
         */
        public fun onRestoreSwipePosition(viewHolder: SwipeViewHolder, adapterPosition: Int)

        public companion object {
            @JvmField
            public val DEFAULT: SwipeListener = object : SwipeListener {
                override fun onSwipeStarted(
                    viewHolder: SwipeViewHolder,
                    adapterPosition: Int,
                    x: Float?,
                    y: Float?,
                ) = Unit

                override fun onSwipeChanged(
                    viewHolder: SwipeViewHolder,
                    adapterPosition: Int,
                    dX: Float,
                    totalDeltaX: Float,
                ) = Unit

                override fun onSwipeCompleted(
                    viewHolder: SwipeViewHolder,
                    adapterPosition: Int,
                    x: Float?,
                    y: Float?,
                ) = Unit

                override fun onSwipeCanceled(
                    viewHolder: SwipeViewHolder,
                    adapterPosition: Int,
                    x: Float?,
                    y: Float?,
                ) = Unit

                override fun onRestoreSwipePosition(viewHolder: SwipeViewHolder, adapterPosition: Int) = Unit
            }
        }
    }
}
