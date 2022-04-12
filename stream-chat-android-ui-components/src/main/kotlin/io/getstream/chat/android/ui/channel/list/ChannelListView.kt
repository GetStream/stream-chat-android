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

package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import com.getstream.sdk.chat.utils.extensions.showToast
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.actions.internal.ChannelActionsDialogFragment
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelClickListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelListItemPredicate
import io.getstream.chat.android.ui.channel.list.ChannelListView.ChannelLongClickListener
import io.getstream.chat.android.ui.channel.list.ChannelListView.ErrorEventHandler
import io.getstream.chat.android.ui.channel.list.ChannelListView.UserClickListener
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder
import io.getstream.chat.android.ui.channel.list.internal.SimpleChannelListView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater

public class ChannelListView : FrameLayout {

    private val CHANNEL_LIST_VIEW_ID = generateViewId()

    private lateinit var emptyStateView: View

    private lateinit var loadingView: View

    private var channelListItemPredicate: ChannelListItemPredicate = ChannelListItemPredicate { true }

    private lateinit var simpleChannelListView: SimpleChannelListView

    // These listeners live here because they are only triggered via ChannelActionsDialogFragment and don't need
    // to be passed to ViewHolders.
    private var channelInfoListener: ChannelClickListener = ChannelClickListener.DEFAULT

    private var channelLeaveListener: ChannelClickListener = ChannelClickListener.DEFAULT

    private var errorEventHandler = ErrorEventHandler { errorEvent ->
        when (errorEvent) {
            is ChannelListViewModel.ErrorEvent.HideChannelError -> R.string.stream_ui_channel_list_error_hide_channel
            is ChannelListViewModel.ErrorEvent.DeleteChannelError -> R.string.stream_ui_channel_list_error_delete_channel
            is ChannelListViewModel.ErrorEvent.LeaveChannelError -> R.string.stream_ui_channel_list_error_leave_channel
        }.let(::showToast)
    }

    private lateinit var style: ChannelListViewStyle
    private lateinit var actionDialogStyle: ChannelActionsDialogViewStyle

    public constructor(context: Context) : this(context, null, 0)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        style = ChannelListViewStyle(context, attrs)

        setBackgroundColor(style.backgroundColor)

        actionDialogStyle = ChannelActionsDialogViewStyle(context, attrs)

        simpleChannelListView = SimpleChannelListView(context, attrs, defStyleAttr)
            .apply {
                id = CHANNEL_LIST_VIEW_ID
                setChannelListViewStyle(style)
            }

        addView(simpleChannelListView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        emptyStateView = streamThemeInflater.inflate(style.emptyStateView, this, false).apply {
            isVisible = false
            addView(this)
        }

        loadingView = streamThemeInflater.inflate(style.loadingView, this, false).apply {
            isVisible = false
            addView(this)
        }

        configureDefaultMoreOptionsListener(context)
    }

    /**
     * @return if the list and its adapter are initialized.
     */
    public fun isAdapterInitialized(): Boolean {
        return ::simpleChannelListView.isInitialized && simpleChannelListView.isAdapterInitialized()
    }

    /**
     * @param view Will be added to the view hierarchy of [ChannelListView] and managed by it.
     * This view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams Defines how the view will be situated inside its container ViewGroup.
     */
    @JvmOverloads
    public fun setEmptyStateView(view: View, layoutParams: LayoutParams = defaultChildLayoutParams) {
        removeView(this.emptyStateView)
        this.emptyStateView = view
        addView(emptyStateView, layoutParams)
    }

    /**
     * @param view Will be added to the view hierarchy of [ChannelListView] and managed by it.
     * This view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams Defines how the view will be situated inside its container ViewGroup.
     */
    @JvmOverloads
    public fun setLoadingView(view: View, layoutParams: LayoutParams = defaultChildLayoutParams) {
        removeView(this.loadingView)
        this.loadingView = view
        addView(loadingView, layoutParams)
    }

    /**
     * Uses the [drawableResource] as the separator for list items.
     *
     * @param drawableResource The drawable used as a separator.
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
     * Allows clients to set a custom implementation of [ChannelListItemViewHolderFactory].
     *
     * @param factory The custom factory to be used when generating item view holders.
     */
    public fun setViewHolderFactory(factory: ChannelListItemViewHolderFactory) {
        simpleChannelListView.setViewHolderFactory(factory)
    }

    /**
     * Allows clients to set a click listener for all channel list items.
     *
     * @param listener The callback to be invoked on channel item click.
     */
    public fun setChannelItemClickListener(listener: ChannelClickListener?) {
        simpleChannelListView.setChannelClickListener(listener)
    }

    /**
     * Allows clients to set a long-click listener for all channel list items.
     *
     * @param listener The callback to be invoked on channel long click.
     */
    public fun setChannelLongClickListener(listener: ChannelLongClickListener?) {
        simpleChannelListView.setChannelLongClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of user click events.
     *
     * @param listener The listener to be invoked when a user click event occurs.
     */
    public fun setUserClickListener(listener: UserClickListener?) {
        simpleChannelListView.setUserClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of delete clicks via channel actions.
     * or view holder swipe menu
     *
     * @param listener The callback to be invoked when delete is clicked.
     */
    public fun setChannelDeleteClickListener(listener: ChannelClickListener?) {
        simpleChannelListView.setChannelDeleteClickListener(listener)
    }

    /**
     * Allows clients to set a click listener to be notified of "more options" clicks in ViewHolder items.
     *
     * @param listener The callback to be invoked when "more options" is clicked.
     */
    public fun setMoreOptionsClickListener(listener: ChannelClickListener?) {
        simpleChannelListView.setMoreOptionsClickListener(listener)
    }

    /**
     * Allows a client to set a click listener to be notified of "channel info" clicks in the "more options" menu.
     *
     * @param listener The callback to be invoked when "channel info" is clicked.
     */
    public fun setChannelInfoClickListener(listener: ChannelClickListener?) {
        channelInfoListener = listener ?: ChannelClickListener.DEFAULT
    }

    /**
     * Allows a client to set a click listener to be notified of "leave channel" clicks in the "more options" menu.
     *
     * @param listener The callback to be invoked when "leave channel" is clicked.
     */
    public fun setChannelLeaveClickListener(listener: ChannelClickListener?) {
        channelLeaveListener = listener ?: ChannelClickListener.DEFAULT
    }

    /**
     * Allows a client to set a swipe listener to be notified of swipe details in order to take action.
     *
     * @param listener The set of functions to be invoked during a swipe's lifecycle.
     */
    public fun setSwipeListener(listener: SwipeListener?) {
        simpleChannelListView.setSwipeListener(listener)
    }

    public fun setOnEndReachedListener(listener: EndReachedListener?) {
        simpleChannelListView.setOnEndReachedListener(listener)
    }

    /**
     * Allows a client to set a ChannelListItemPredicate to filter ChannelListItems before they are drawn.
     *
     * @param channelListItemPredicate Predicate used to filter the list of ChannelListItem.
     */
    public fun setChannelListItemPredicate(channelListItemPredicate: ChannelListItemPredicate) {
        this.channelListItemPredicate = channelListItemPredicate
        simpleChannelListView.currentChannelItemList()?.let(::setChannels)
    }

    public fun setErrorEventHandler(handler: ErrorEventHandler) {
        this.errorEventHandler = handler
    }

    public fun showError(errorEvent: ChannelListViewModel.ErrorEvent) {
        errorEventHandler.onErrorEvent(errorEvent)
    }

    public fun setChannels(channels: List<ChannelListItem>) {
        val filteredChannels = channels.filter(channelListItemPredicate::predicate)

        if (filteredChannels.isEmpty()) {
            showEmptyStateView()
        } else {
            hideEmptyStateView()
        }

        simpleChannelListView.setChannels(filteredChannels)
    }

    public fun hideLoadingView() {
        this.loadingView.isVisible = false
    }

    public fun showLoadingView() {
        hideEmptyStateView()
        this.loadingView.isVisible = true
    }

    public fun showLoadingMore() {
        this.simpleChannelListView.showLoadingMore(true)
    }

    public fun hideLoadingMore() {
        this.simpleChannelListView.showLoadingMore(false)
    }

    private fun showEmptyStateView() {
        this.emptyStateView.isVisible = true
    }

    private fun hideEmptyStateView() {
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

    private fun configureDefaultMoreOptionsListener(context: Context) {
        setMoreOptionsClickListener { channel ->
            context.getFragmentManager()?.let { fragmentManager ->
                ChannelActionsDialogFragment
                    .newInstance(channel.cid, !channel.isDirectMessaging(), actionDialogStyle)
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
         * @return True if the callback consumed the long click, false otherwise.
         */
        public fun onLongClick(channel: Channel): Boolean
    }

    public fun interface EndReachedListener {
        public fun onEndReached()
    }

    /**
     * Predicate object with a filter condition for ChannelListItem. Used to filter a list of ChannelListItem
     * before applying it to ChannelListView.
     */
    public fun interface ChannelListItemPredicate {
        /**
         * Should return true for items that should be kept after filtering.
         */
        public fun predicate(channelListItem: ChannelListItem): Boolean
    }

    public fun interface ErrorEventHandler {
        public fun onErrorEvent(errorEvent: ChannelListViewModel.ErrorEvent)
    }

    public interface SwipeListener {
        /**
         * Invoked when a swipe is detected.
         *
         * @param viewHolder The view holder that is being swiped.
         * @param adapterPosition The internal adapter position of the item being bound.
         * @param x The raw X of the swipe origin; null may indicate the call isn't from user interaction.
         * @param y The raw Y of the swipe origin; null may indicate the call isn't from user interaction.
         */
        public fun onSwipeStarted(viewHolder: SwipeViewHolder, adapterPosition: Int, x: Float? = null, y: Float? = null)

        /**
         * Invoked after a swipe has been detected, and movement is occurring.
         *
         * @param viewHolder The view holder that is being swiped.
         * @param adapterPosition The internal adapter position of the item being bound.
         * @param dX The change from the previous swipe touch event to the current.
         * @param totalDeltaX The change from the first touch event to the current.
         */
        public fun onSwipeChanged(viewHolder: SwipeViewHolder, adapterPosition: Int, dX: Float, totalDeltaX: Float)

        /**
         * Invoked when a swipe is successfully completed naturally, without cancellation.
         *
         * @param viewHolder The view holder that is being swiped.
         * @param adapterPosition The internal adapter position of the item being bound.
         * @param x The raw X of the swipe origin; null may indicate the call isn't from user interaction.
         * @param y The raw Y of the swipe origin; null may indicate the call isn't from user interaction.
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
         * @param viewHolder The view holder that is being swiped.
         * @param adapterPosition The internal adapter position of the item being bound.
         * @param x The raw X of the swipe origin; null may indicate the call isn't from user interaction.
         * @param y The raw Y of the swipe origin; null may indicate the call isn't from user interaction.
         */
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
         * @param viewHolder The view holder being bound.
         * @param adapterPosition The internal adapter position of the item being bound.
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
