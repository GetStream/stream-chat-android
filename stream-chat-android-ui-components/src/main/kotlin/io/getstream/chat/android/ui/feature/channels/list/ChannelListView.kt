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

package io.getstream.chat.android.ui.feature.channels.list

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import io.getstream.chat.android.ui.common.state.channels.actions.LeaveGroup
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo
import io.getstream.chat.android.ui.feature.channels.actions.ChannelActionsDialogViewStyle
import io.getstream.chat.android.ui.feature.channels.actions.internal.ChannelActionsDialogFragment
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView.ChannelClickListener
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView.ChannelListItemPredicate
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView.ChannelListUpdateListener
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView.ChannelLongClickListener
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView.ErrorEventHandler
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView.UserClickListener
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.SwipeViewHolder
import io.getstream.chat.android.ui.feature.channels.list.internal.SimpleChannelListView
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import io.getstream.chat.android.ui.utils.extensions.showToast
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.widgets.internal.ScrollPauseLinearLayoutManager

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

    /**
     * A listener that will be notified once the channel list is updated with the new data set. By default will scroll
     * the list to the bottom if it is at the end and the [ChannelListItem.LoadingMoreItem] is inside the list.
     */
    private var channelListUpdateListener: ChannelListUpdateListener? = ChannelListUpdateListener { items ->
        (layoutManager as? ScrollPauseLinearLayoutManager)?.let { layoutManager ->
            if (items.contains(ChannelListItem.LoadingMoreItem) &&
                layoutManager.findLastVisibleItemPosition() in items.size - 2..items.size
            ) {
                layoutManager.scrollToPosition(items.size - 1)
            }
        }
    }

    /**
     * The pending scroll state that we need to restore.
     */
    private var layoutManagerState: Parcelable? = null

    /**
     * The layout manager of the inner RecyclerView.
     */
    private val layoutManager: RecyclerView.LayoutManager?
        get() = if (::simpleChannelListView.isInitialized) {
            simpleChannelListView.layoutManager
        } else {
            null
        }

    public constructor(context: Context) : this(context, null, 0)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
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
     * Returns the inner [RecyclerView] that is used to display a list of channel list items.
     *
     * @return The inner [RecyclerView] with channels.
     */
    public fun getRecyclerView(): RecyclerView = simpleChannelListView

    /**
     * Returns [LinearLayoutManager] associated with the inner [RecyclerView].
     *
     * @return [LinearLayoutManager] associated with the inner [RecyclerView]
     */
    public fun getLayoutManager(): LinearLayoutManager? = layoutManager as? LinearLayoutManager

    override fun onSaveInstanceState(): Parcelable = bundleOf(
        KEY_SUPER_STATE to super.onSaveInstanceState(),
        KEY_SCROLL_STATE to layoutManager?.onSaveInstanceState(),
    )

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }

        layoutManagerState = state.getParcelable(KEY_SCROLL_STATE)
        super.onRestoreInstanceState(state.getParcelable(KEY_SUPER_STATE))
    }

    /**
     * Restores the scroll state based on the persisted
     */
    private fun restoreLayoutManagerState() {
        if (layoutManagerState != null) {
            layoutManager?.onRestoreInstanceState(layoutManagerState)
            layoutManagerState = null
        }
    }

    /**
     * @return if the list and its adapter are initialized.
     */
    public fun isAdapterInitialized(): Boolean = ::simpleChannelListView.isInitialized && simpleChannelListView.isAdapterInitialized()

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
     * Allows clients to set a visibility controller for the "more options" icon in ViewHolder items.
     *
     * @param isMoreOptionsVisible The callback to be invoked when the visibility of "more options" gets checked.
     */
    public fun setIsMoreOptionsVisible(isMoreOptionsVisible: (Channel) -> Boolean) {
        simpleChannelListView.setIsMoreOptionsVisible(isMoreOptionsVisible)
    }

    /**
     * Allows clients to set a visibility controller for the "delete option" icon in ViewHolder items.
     *
     * @param isDeleteOptionVisible The callback to be invoked when the visibility of "delete option" gets checked.
     */
    public fun setIsDeleteOptionVisible(isDeleteOptionVisible: (Channel) -> Boolean) {
        simpleChannelListView.setIsDeleteOptionVisible(isDeleteOptionVisible)
    }

    /**
     * Allows clients to override a "more options" icon in ViewHolder items.
     *
     * @param getMoreOptionsIcon Provides icon for a "more options".
     */
    public fun setMoreOptionsIconProvider(getMoreOptionsIcon: (Channel) -> Drawable?) {
        simpleChannelListView.setMoreOptionsIconProvider(getMoreOptionsIcon)
    }

    /**
     * Allows clients to override a "delete option" icon in ViewHolder items.
     *
     * @param getDeleteOptionIcon Provides icon for delete option.
     */
    public fun setDeleteOptionIconProvider(getDeleteOptionIcon: (Channel) -> Drawable?) {
        simpleChannelListView.setDeleteOptionIconProvider(getDeleteOptionIcon)
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
     * Allow a client to set a listener to be notified when the updated channel list is about to be displayed.
     *
     * @param listener The callback to be invoked when the new channel list that is about to be displayed.
     */
    public fun setChannelListUpdateListener(listener: ChannelListUpdateListener) {
        channelListUpdateListener = listener
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

        simpleChannelListView.setChannels(filteredChannels) {
            restoreLayoutManagerState()
            channelListUpdateListener?.onChannelListUpdate(filteredChannels)
        }
    }

    public fun hideLoadingView() {
        this.loadingView.isVisible = false
    }

    public fun showLoadingView() {
        hideEmptyStateView()
        this.loadingView.isVisible = true
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

    public fun hasChannels(): Boolean = simpleChannelListView.hasChannels()

    private companion object {
        private val defaultChildLayoutParams: LayoutParams by lazy {
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            )
        }

        private const val KEY_SUPER_STATE = "super_state"
        private const val KEY_SCROLL_STATE = "scroll_state"
    }

    private fun configureDefaultMoreOptionsListener(context: Context) {
        setMoreOptionsClickListener { channel ->
            context.getFragmentManager()?.let { fragmentManager ->
                ChannelActionsDialogFragment
                    .newInstance(channel, actionDialogStyle)
                    .apply {
                        setChannelOptionClickListener { channelAction ->
                            when (channelAction) {
                                is ViewInfo -> channelInfoListener.onClick(channelAction.channel)
                                is LeaveGroup -> channelLeaveListener.onClick(channelAction.channel)
                                is DeleteConversation -> {
                                    simpleChannelListView.listenerContainer
                                        .deleteClickListener
                                        .onClick(channelAction.channel)
                                }
                                else -> Unit
                            }
                        }
                        setChannelMemberClickListener { member ->
                            simpleChannelListView.listenerContainer
                                .userClickListener
                                .onClick(member.user)
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

    public fun interface ChannelOptionVisibilityPredicate : Function1<Channel, Boolean> {
        public companion object {
            @JvmField
            public val DEFAULT: ChannelOptionVisibilityPredicate = ChannelOptionVisibilityPredicate {
                // option is visible by default
                true
            }
        }

        /**
         * Called to check option's visibility for the specified [channel].
         *
         * @return True if the option is visible.
         */
        override fun invoke(channel: Channel): Boolean
    }

    public fun interface ChannelOptionIconProvider : Function1<Channel, Drawable?> {

        public companion object {
            @JvmField
            public val DEFAULT: ChannelOptionIconProvider = ChannelOptionIconProvider {
                // option has no customized icon by default
                null
            }
        }

        /**
         * Called to provide option's icon for the specified [channel].
         *
         * @return Drawable which overrides ChannelListViewStyle values.
         */
        override fun invoke(channel: Channel): Drawable?
    }

    public fun interface EndReachedListener {
        public fun onEndReached()
    }

    /**
     * Called when the updated list is about to be displayed in the channels [RecyclerView].
     */
    public fun interface ChannelListUpdateListener {
        /**
         * Called when the updated list is about to be displayed in the channels [RecyclerView].
         *
         * @param channels The new channel list that is about to be displayed.
         */
        public fun onChannelListUpdate(channels: List<ChannelListItem>)
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
