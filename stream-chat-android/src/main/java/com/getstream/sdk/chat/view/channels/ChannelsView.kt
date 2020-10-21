package com.getstream.sdk.chat.view.channels

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.ChannelViewHolderFactory
import com.getstream.sdk.chat.view.channels.ChannelListView.ChannelClickListener
import io.getstream.chat.android.client.models.Channel

private val LOADING_VIEW_ID = R.id.channels_loading_view_id
private val EMPTY_STATE_VIEW_ID = R.id.channels_empty_state_view_id
private val CHANNEL_LIST_VIEW_ID = R.id.channels_list_view_id

public class ChannelsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var emptyStateView: View = defaultEmptyStateView()
    private var loadingView: View = defaultLoadingView()
    private val channelListView: ChannelListView = ChannelListView(context, attrs, defStyleAttr).apply { id = CHANNEL_LIST_VIEW_ID }

    init {
        addView(channelListView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        emptyStateView.apply {
            isVisible = false
            addView(this, defaultChildLayoutParams())
        }
        loadingView.apply {
            isVisible = false
            addView(loadingView, defaultChildLayoutParams())
        }
    }

    public fun setEmptyStateView(view: View, layoutParams: LayoutParams = defaultChildLayoutParams()) {
        removeView(this.emptyStateView)
        this.emptyStateView = view.apply { id = EMPTY_STATE_VIEW_ID }
        addView(emptyStateView, layoutParams)
    }

    public fun setLoadingView(view: View, layoutParams: LayoutParams = defaultChildLayoutParams()) {
        removeView(this.loadingView)
        this.loadingView = view.apply { id = LOADING_VIEW_ID }
        addView(loadingView, layoutParams)
    }

    public fun setViewHolderFactory(factory: ChannelViewHolderFactory) {
        this.channelListView.setViewHolderFactory(factory)
    }

    public fun setOnChannelClickListener(listener: (Channel) -> Unit) {
        channelListView.setOnChannelClickListener(listener)
    }

    public fun setOnLongClickListener(listener: ChannelClickListener) {
        channelListView.setOnLongClickListener(listener)
    }

    public fun setOnEndReachedListener(listener: () -> Unit) {
        channelListView.setOnEndReachedListener(listener)
    }

    public fun setChannels(channels: List<Channel>) {
        channelListView.setChannels(channels)
    }

    public fun hideLoadingView() {
        this.loadingView.isVisible = false
    }

    public fun showLoadingView() {
        this.loadingView.isVisible = true
    }

    public fun showEmptyStateView() {
        this.emptyStateView.isVisible = true
    }

    public fun hideEmptyStateView() {
        this.emptyStateView.isVisible = false
    }

    private fun defaultChildLayoutParams() =
        LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)

    private fun defaultLoadingView(): View = ProgressBar(context).apply {
        id = LOADING_VIEW_ID
    }

    private fun defaultEmptyStateView(): View = TextView(context).apply {
        setText(R.string.stream_channels_empty_state_label)
        id = EMPTY_STATE_VIEW_ID
    }
}
