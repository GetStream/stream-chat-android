package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelClickListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.UserClickListener
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItemAdapter
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.cast

public class ChannelListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var endReachedListener: EndReachedListener? = null
    private val layoutManager: LinearLayoutManager
    private val scrollListener: EndReachedScrollListener = EndReachedScrollListener()
    private val dividerDecoration: SimpleVerticalListDivider = SimpleVerticalListDivider()

    init {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        setLayoutManager(layoutManager)
        adapter = ChannelListItemAdapter()
        parseStyleAttributes(context, attrs)
        addItemDecoration(dividerDecoration)
    }

    private fun parseStyleAttributes(context: Context, attrs: AttributeSet?) {
        // parse the attributes
        requireAdapter().style = ChannelListViewStyle(context, attrs)
    }

    private fun requireAdapter(): ChannelListItemAdapter {
        val logger = ChatLogger.get("ChannelListView::requireAdapter")
        val channelAdapter = adapter

        require(channelAdapter != null) {
            logger.logE("Required adapter was null")
        }

        require(channelAdapter is ChannelListItemAdapter) {
            logger.logE("Adapter must be an instance of ChannelListItemAdapter")
        }

        return channelAdapter.cast()
    }

    private fun canScrollUpForChannelEvent(): Boolean = layoutManager.findFirstVisibleItemPosition() < 3

    public fun setViewHolderFactory(factory: ChannelListItemViewHolderFactory) {
        requireAdapter().viewHolderFactory = factory
    }

    public fun setChannelClickListener(listener: ChannelClickListener?) {
        requireAdapter().listenerProxy.channelClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setChannelLongClickListener(listener: ChannelClickListener?) {
        requireAdapter().listenerProxy.channelLongClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setUserClickListener(listener: UserClickListener?) {
        requireAdapter().listenerProxy.userClickListener = listener ?: UserClickListener.DEFAULT
    }

    public fun setChannelDeleteListener(listener: ChannelClickListener?) {
        requireAdapter().listenerProxy.deleteClickListener = listener ?: ChannelClickListener.DEFAULT
    }

    public fun setItemSeparator(@DrawableRes drawableResource: Int) {
        dividerDecoration.drawableResource = drawableResource
    }

    public fun setItemSeparatorHeight(height: Int) {
        dividerDecoration.drawableHeight = height
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

    public fun reachedEndOfChannels(endReached: Boolean) {
        requireAdapter().endReached = endReached
    }

    public fun setChannels(channels: List<Channel>) {
        requireAdapter().submitList(channels)
    }

    public override fun onVisibilityChanged(view: View, visibility: Int) {
        super.onVisibilityChanged(view, visibility)
        if (visibility == 0 && adapter != null) requireAdapter().notifyDataSetChanged()
    }

    public fun interface UserClickListener {
        public companion object {
            public val DEFAULT: UserClickListener = UserClickListener {}
        }

        public fun onUserClick(user: User)
    }

    public fun interface ChannelClickListener {
        public companion object {
            public val DEFAULT: ChannelClickListener = ChannelClickListener {}
        }

        public fun onClick(channel: Channel)
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
}
