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
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.actions.internal.ChannelActionsDialogFragment
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder
import io.getstream.chat.android.ui.channel.list.internal.SimpleChannelListView
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager

public class ChannelListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val CHANNEL_LIST_VIEW_ID = generateViewId()

    private var emptyStateView: View = defaultEmptyStateView()

    private var loadingView: View = defaultLoadingView()

    private val simpleChannelListView: SimpleChannelListView =
        SimpleChannelListView(context, attrs, defStyleAttr).apply { id = CHANNEL_LIST_VIEW_ID }

    // These listeners live here because they are only triggered via ChannelActionsDialogFragment and don't need
    // to be passed to ViewHolders.
    private var channelInfoListener: ChannelClickListener = ChannelClickListener.DEFAULT

    private var channelLeaveListener: ChannelClickListener = ChannelClickListener.DEFAULT

    init {
        addView(simpleChannelListView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

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
        context.obtainStyledAttributes(attrs, R.styleable.ChannelListView, 0, 0).use {
            it.getResourceId(
                R.styleable.ChannelListView_streamUiChannelsItemSeparatorDrawable,
                R.drawable.stream_ui_divider
            )
                .let { separator ->
                    simpleChannelListView.setItemSeparator(separator)
                }
        }
    }

    /**
     * @param view will be added to the view hierarchy of [ChannelListView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams defines how the view will be situated inside its container ViewGroup.
     */
    @JvmOverloads
    public fun setEmptyStateView(view: View, layoutParams: LayoutParams = defaultChildLayoutParams) {
        removeView(this.emptyStateView)
        this.emptyStateView = view
        addView(emptyStateView, layoutParams)
    }

    /**
     * @param view will be added to the view hierarchy of [ChannelListView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams defines how the view will be situated inside its container ViewGroup.
     */
    @JvmOverloads
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
        simpleChannelListView.setItemSeparator(drawableResource)
    }

    public fun setItemSeparatorHeight(dp: Int) {
        simpleChannelListView.setItemSeparatorHeight(dp.dpToPx())
    }

    public fun setShouldDrawItemSeparatorOnLastItem(shouldDrawOnLastItem: Boolean) {
        simpleChannelListView.setShouldDrawItemSeparatorOnLastItem(shouldDrawOnLastItem)
    }

    /**
     * Allows clients to set a custom implementation of [BaseChannelViewHolderFactory]
     *
     * @param factory the custom factory to be used when generating item view holders
     */
    public fun setViewHolderFactory(factory: ChannelListItemViewHolderFactory) {
        simpleChannelListView.setViewHolderFactory(factory)
    }

    /**
     * Allows clients to set a click listener for all channel list items
     *
     * @param listener the callback to be invoked on channel item click
     */
    public fun setChannelItemClickListener(listener: ChannelClickListener?) {
        simpleChannelListView.setChannelClickListener(listener)
    }

    /**
     * Allows clients to set a long-click listener for all channel list items
     *
     * @param listener the callback to be invoked on channel long click
     */
    public fun setChannelLongClickListener(listener: ChannelLongClickListener?) {
        simpleChannelListView.setChannelLongClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of user click events
     *
     * @param listener the listener to be invoked when a user click event occurs
     */
    public fun setUserClickListener(listener: UserClickListener?) {
        simpleChannelListView.setUserClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of delete clicks via channel actions
     * or view holder swipe menu
     *
     * @param listener - the callback to be invoked when delete is clicked
     */
    public fun setChannelDeleteClickListener(listener: ChannelClickListener?) {
        simpleChannelListView.setChannelDeleteClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of "more options" clicks in ViewHolder items
     *
     * @param listener - the callback to be invoked when "more options" is clicked
     */
    public fun setMoreOptionsClickListener(listener: ChannelClickListener?) {
        simpleChannelListView.setMoreOptionsClickListener(listener)
    }

    /**
     * Allows a client to set a click listener to be notified of "channel info" clicks in the "more options" menu
     *
     * @param listener - the callback to be invoked when "channel info" is clicked
     */
    public fun setChannelInfoClickListener(listener: ChannelClickListener?) {
        channelInfoListener = listener ?: ChannelClickListener.DEFAULT
    }

    /**
     * Allows a client to set a click listener to be notified of "leave channel" clicks in the "more options" menu
     *
     * @param listener - the callback to be invoked when "leave channel" is clicked
     */
    public fun setChannelLeaveClickListener(listener: ChannelClickListener?) {
        channelLeaveListener = listener ?: ChannelClickListener.DEFAULT
    }

    /**
     * Allows a client to set a swipe listener to be notified of swipe details in order to take action
     *
     * @param listener - the set of functions to be invoked during a swipe's lifecycle
     */
    public fun setSwipeListener(listener: SwipeListener?) {
        simpleChannelListView.setSwipeListener(listener)
    }

    public fun setOnEndReachedListener(listener: EndReachedListener?) {
        simpleChannelListView.setOnEndReachedListener(listener)
    }

    public fun setChannels(channels: List<ChannelListItem>) {
        simpleChannelListView.setChannels(channels)
    }

    public fun hideLoadingView() {
        this.loadingView.isVisible = false
    }

    public fun showLoadingView() {
        this.loadingView.isVisible = true
    }

    public fun showLoadingMore() {
        this.simpleChannelListView.showLoadingMore(true)
    }

    public fun hideLoadingMore() {
        this.simpleChannelListView.showLoadingMore(false)
    }

    public fun showEmptyStateView() {
        this.emptyStateView.isVisible = true
    }

    public fun hideEmptyStateView() {
        this.emptyStateView.isVisible = false
    }

    public fun setPaginationEnabled(enabled: Boolean) {
        simpleChannelListView.setPaginationEnabled(enabled)
    }

    public fun hasChannels(): Boolean {
        return simpleChannelListView.hasChannels()
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
                                simpleChannelListView.listenerContainer.deleteClickListener.onClick(
                                    simpleChannelListView.getChannel(cid)
                                )
                            }

                            override fun onLeaveChannelClicked(cid: String) {
                                channelLeaveListener.onClick(
                                    simpleChannelListView.getChannel(cid)
                                )
                            }

                            override fun onMemberSelected(member: Member) {
                                simpleChannelListView.listenerContainer.userClickListener.onClick(member.user)
                            }

                            override fun onChannelInfoSelected(cid: String) {
                                channelInfoListener.onClick(
                                    simpleChannelListView.getChannel(cid)
                                )
                            }
                        }
                    }
                    .show(fragmentManager, null)
            }
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
