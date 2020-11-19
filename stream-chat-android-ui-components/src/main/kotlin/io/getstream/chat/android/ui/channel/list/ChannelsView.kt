package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.use
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.dpToPx

public class ChannelsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val CHANNEL_LIST_VIEW_ID = R.id.stream_channels_list_view_id

    private var emptyStateView: View = defaultEmptyStateView()
    private var loadingView: View = defaultLoadingView()
    private val channelListView: ChannelListView =
        ChannelListView(context, attrs, defStyleAttr).apply { id = CHANNEL_LIST_VIEW_ID }

    init {
        addView(channelListView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        emptyStateView.apply {
            isVisible = false
            addView(this, defaultChildLayoutParams)
        }
        loadingView.apply {
            isVisible = false
            addView(loadingView, defaultChildLayoutParams)
        }
        parseAttrs(attrs)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ChannelsView, 0, 0).use {
            it.getText(R.styleable.ChannelsView_streamChannelsEmptyStateLabelText)?.let { emptyStateText ->
                emptyStateView.apply {
                    if (this is TextView) {
                        text = emptyStateText
                    }
                }
            }

            it.getResourceId(R.styleable.ChannelsView_streamChannelsItemSeparatorDrawable, R.drawable.stream_divider)
                .let { separator ->
                    channelListView.setItemSeparator(separator)
                }
        }
    }

    /**
     * @param view will be added to the view hierarchy of [ChannelsView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams defines how the view will be situated inside its container ViewGroup.
     */
    public fun setEmptyStateView(view: View, layoutParams: LayoutParams = defaultChildLayoutParams) {
        removeView(this.emptyStateView)
        this.emptyStateView = view
        addView(emptyStateView, layoutParams)
    }

    /**
     * @param view will be added to the view hierarchy of [ChannelsView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams defines how the view will be situated inside its container ViewGroup.
     */
    public fun setLoadingView(view: View, layoutParams: LayoutParams = defaultChildLayoutParams) {
        removeView(this.loadingView)
        this.loadingView = view
        addView(loadingView, layoutParams)
    }

    /**
     * Uses the [drawableResource] as the separator for list items
     *
     * @param drawableResource the drawable used as a separator
     */
    public fun setItemSeparator(@DrawableRes drawableResource: Int) {
        channelListView.setItemSeparator(drawableResource)
    }

    public fun setItemSeparatorHeight(dp: Int) {
        channelListView.setItemSeparatorHeight(dp.dpToPx())
    }

    /**
     * Allows clients to set a custom implementation of [BaseChannelViewHolderFactory]
     *
     * @param factory the custom factory to be used when generating item view holders
     */
    public fun setViewHolderFactory(factory: BaseChannelViewHolderFactory<BaseChannelListItemViewHolder>) {
        channelListView.setViewHolderFactory(factory)
    }

    /**
     * Allows clients to set a click listener for all channel list items
     *
     * @param listener the callback to be invoked on channel item click
     */
    public fun setChannelClickListener(listener: ChannelListView.ChannelClickListener) {
        channelListView.setChannelClickListener(listener)
    }

    /**
     * Allows clients to set a long-click listener for all channel list items
     *
     * @param listener the callback to be invoked on channel long click
     */
    public fun setChannelLongClickListener(listener: ChannelListView.ChannelClickListener) {
        channelListView.setChannelLongClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of user click events
     *
     * @param listener the listener to be invoked when a user click event occurs
     */
    public fun setUserClickListener(listener: ChannelListView.UserClickListener) {
        channelListView.setUserClickListener(listener)
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

    public fun setPaginationEnabled(enabled: Boolean) {
        channelListView.setPaginationEnabled(enabled)
    }

    private companion object {
        private val defaultChildLayoutParams: LayoutParams by lazy {
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        }
    }

    private fun defaultLoadingView(): View = ProgressBar(context)

    private fun defaultEmptyStateView(): View = TextView(context).apply {
        setText(R.string.stream_channels_empty_state_label)
    }
}
