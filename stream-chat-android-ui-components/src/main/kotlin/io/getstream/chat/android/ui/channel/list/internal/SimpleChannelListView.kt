package io.getstream.chat.android.ui.channel.list.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.internal.ChannelListItemAdapter
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal.ChannelItemSwipeListener
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal.ChannelListListenerContainerImpl
import io.getstream.chat.android.ui.common.extensions.internal.cast

internal class SimpleChannelListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : RecyclerView(context, attrs, defStyle) {

    private val layoutManager: ScrollPauseLinearLayoutManager
    private val scrollListener: EndReachedScrollListener = EndReachedScrollListener()
    private val dividerDecoration: SimpleVerticalListDivider = SimpleVerticalListDivider()

    private var endReachedListener: ChannelListView.EndReachedListener? = null

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

    fun setViewHolderFactory(viewHolderFactory: ChannelListItemViewHolderFactory) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set ChannelListItemViewHolderFactory first" }

        this.viewHolderFactory = viewHolderFactory
    }

    fun setChannelClickListener(listener: ChannelListView.ChannelClickListener?) {
        listenerContainer.channelClickListener = listener ?: ChannelListView.ChannelClickListener.DEFAULT
    }

    fun setChannelLongClickListener(listener: ChannelListView.ChannelLongClickListener?) {
        listenerContainer.channelLongClickListener = listener ?: ChannelListView.ChannelLongClickListener.DEFAULT
    }

    fun setUserClickListener(listener: ChannelListView.UserClickListener?) {
        listenerContainer.userClickListener = listener ?: ChannelListView.UserClickListener.DEFAULT
    }

    fun setChannelDeleteClickListener(listener: ChannelListView.ChannelClickListener?) {
        listenerContainer.deleteClickListener = listener ?: ChannelListView.ChannelClickListener.DEFAULT
    }

    fun setMoreOptionsClickListener(listener: ChannelListView.ChannelClickListener?) {
        listenerContainer.moreOptionsClickListener = listener ?: ChannelListView.ChannelClickListener.DEFAULT
    }

    fun setSwipeListener(listener: ChannelListView.SwipeListener?) {
        listenerContainer.swipeListener = listener ?: ChannelListView.SwipeListener.DEFAULT
    }

    fun setItemSeparator(@DrawableRes drawableResource: Int) {
        dividerDecoration.drawableResource = drawableResource
    }

    fun setItemSeparatorHeight(height: Int) {
        dividerDecoration.drawableHeight = height
    }

    fun setShouldDrawItemSeparatorOnLastItem(shouldDrawOnLastItem: Boolean) {
        dividerDecoration.drawOnLastItem = shouldDrawOnLastItem
    }

    fun setOnEndReachedListener(listener: ChannelListView.EndReachedListener?) {
        endReachedListener = listener
        observeListEndRegion()
    }

    private fun observeListEndRegion() {
        addOnScrollListener(scrollListener)
    }

    fun setPaginationEnabled(enabled: Boolean) {
        scrollListener.setPaginationEnabled(enabled)
    }

    fun setChannels(channels: List<ChannelListItem>) {
        requireAdapter().submitList(channels)
    }

    fun showLoadingMore(show: Boolean) {
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

    fun hasChannels(): Boolean {
        return requireAdapter().itemCount > 0
    }

    internal fun getChannel(cid: String): Channel = adapter.getChannel(cid)

    override fun onVisibilityChanged(view: View, visibility: Int) {
        super.onVisibilityChanged(view, visibility)
        if (visibility == View.VISIBLE && ::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
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
}
