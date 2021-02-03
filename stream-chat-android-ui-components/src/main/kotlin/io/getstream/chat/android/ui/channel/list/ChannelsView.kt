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
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.actions.ChannelActionsDialogFragment
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager

public class ChannelsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val CHANNEL_LIST_VIEW_ID = generateViewId()

    private var emptyStateView: View = defaultEmptyStateView()

    private var loadingView: View = defaultLoadingView()

    private val channelListView: ChannelListView =
        ChannelListView(context, attrs, defStyleAttr).apply { id = CHANNEL_LIST_VIEW_ID }

    // These listeners live here because they are only triggered via ChannelActionsDialogFragment and don't need
    // to be passed to ViewHolders.
    private var channelInfoListener: ChannelListView.ChannelClickListener = ChannelListView.ChannelClickListener.DEFAULT

    private var channelLeaveListener: ChannelListView.ChannelClickListener =
        ChannelListView.ChannelClickListener.DEFAULT

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

        configureDefaultMoreOptionsListener(context)

        parseAttrs(attrs)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ChannelsView, 0, 0).use {
            it.getResourceId(
                R.styleable.ChannelsView_streamUiChannelsItemSeparatorDrawable,
                R.drawable.stream_ui_divider
            )
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

    public fun setShouldDrawItemSeparatorOnLastItem(shouldDrawOnLastItem: Boolean) {
        channelListView.setShouldDrawItemSeparatorOnLastItem(shouldDrawOnLastItem)
    }

    /**
     * Allows clients to set a custom implementation of [BaseChannelViewHolderFactory]
     *
     * @param factory the custom factory to be used when generating item view holders
     */
    public fun setViewHolderFactory(factory: ChannelListItemViewHolderFactory) {
        channelListView.setViewHolderFactory(factory)
    }

    /**
     * Allows clients to set a click listener for all channel list items
     *
     * @param listener the callback to be invoked on channel item click
     */
    public fun setChannelItemClickListener(listener: ChannelListView.ChannelClickListener?) {
        channelListView.setChannelClickListener(listener)
    }

    /**
     * Allows clients to set a long-click listener for all channel list items
     *
     * @param listener the callback to be invoked on channel long click
     */
    public fun setChannelLongClickListener(listener: ChannelListView.ChannelLongClickListener?) {
        channelListView.setChannelLongClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of user click events
     *
     * @param listener the listener to be invoked when a user click event occurs
     */
    public fun setUserClickListener(listener: ChannelListView.UserClickListener?) {
        channelListView.setUserClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of delete clicks via channel actions
     * or view holder swipe menu
     *
     * @param listener - the callback to be invoked when delete is clicked
     */
    public fun setChannelDeleteClickListener(listener: ChannelListView.ChannelClickListener?) {
        channelListView.setChannelDeleteClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of "more options" clicks in ViewHolder items
     *
     * @param listener - the callback to be invoked when "more options" is clicked
     */
    public fun setMoreOptionsClickListener(listener: ChannelListView.ChannelClickListener?) {
        channelListView.setMoreOptionsClickListener(listener)
    }

    /**
     * Allows a client to set a click listener to be notified of "channel info" clicks in the "more options" menu
     *
     * @param listener - the callback to be invoked when "channel info" is clicked
     */
    public fun setChannelInfoClickListener(listener: ChannelListView.ChannelClickListener?) {
        channelInfoListener = listener ?: ChannelListView.ChannelClickListener.DEFAULT
    }

    /**
     * Allows a client to set a click listener to be notified of "leave channel" clicks in the "more options" menu
     *
     * @param listener - the callback to be invoked when "leave channel" is clicked
     */
    public fun setChannelLeaveClickListener(listener: ChannelListView.ChannelClickListener?) {
        channelLeaveListener = listener ?: ChannelListView.ChannelClickListener.DEFAULT
    }

    /**
     * Allows a client to set a swipe listener to be notified of swipe details in order to take action
     *
     * @param listener - the set of functions to be invoked during a swipe's lifecycle
     */
    public fun setSwipeListener(listener: ChannelListView.SwipeListener?) {
        channelListView.setSwipeListener(listener)
    }

    public fun setOnEndReachedListener(listener: ChannelListView.EndReachedListener?) {
        channelListView.setOnEndReachedListener(listener)
    }

    public fun setChannels(channels: List<ChannelListItem>) {
        channelListView.setChannels(channels)
    }

    public fun hideLoadingView() {
        this.loadingView.isVisible = false
    }

    public fun showLoadingView() {
        this.loadingView.isVisible = true
    }

    public fun showLoadingMore() {
        this.channelListView.showLoadingMore(true)
    }

    public fun hideLoadingMore() {
        this.channelListView.showLoadingMore(false)
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

    public fun hasChannels(): Boolean {
        return channelListView.hasChannels()
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
        setText(R.string.stream_ui_channels_empty_state_label)
    }

    private fun configureDefaultMoreOptionsListener(
        context: Context,
    ) {
        setMoreOptionsClickListener { channel ->
            context.getFragmentManager()?.let { fragmentManager ->
                ChannelActionsDialogFragment
                    .newInstance(channel.cid, !channel.isDirectMessaging())
                    .apply {
                        channelActionListener = object : ChannelActionsDialogFragment.ChannelActionListener {
                            override fun onDeleteConversationClicked(cid: String) {
                                channelListView.listenerContainer.deleteClickListener.onClick(
                                    channelListView.getChannel(cid)
                                )
                            }

                            override fun onLeaveChannelClicked(cid: String) {
                                channelLeaveListener.onClick(
                                    channelListView.getChannel(cid)
                                )
                            }

                            override fun onMemberSelected(member: Member) {
                                channelListView.listenerContainer.userClickListener.onClick(member.user)
                            }

                            override fun onChannelInfoSelected(cid: String) {
                                channelInfoListener.onClick(
                                    channelListView.getChannel(cid)
                                )
                            }
                        }
                    }
                    .show(fragmentManager, null)
            }
        }
    }
}
