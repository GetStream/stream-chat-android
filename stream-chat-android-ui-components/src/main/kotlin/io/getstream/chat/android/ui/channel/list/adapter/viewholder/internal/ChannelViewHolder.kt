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

package io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import com.getstream.sdk.chat.utils.formatDate
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.isAnonymousChannel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder
import io.getstream.chat.android.ui.common.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.common.extensions.getLastMessage
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getLastMessagePreviewText
import io.getstream.chat.android.ui.common.extensions.internal.isMessageRead
import io.getstream.chat.android.ui.common.extensions.internal.isMuted
import io.getstream.chat.android.ui.common.extensions.internal.isNotNull
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemBackgroundViewBinding
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemForegroundViewBinding
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemViewBinding
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout
import kotlin.math.absoluteValue

internal class ChannelViewHolder @JvmOverloads constructor(
    parent: ViewGroup,
    private val channelClickListener: ChannelListView.ChannelClickListener,
    private val channelLongClickListener: ChannelListView.ChannelLongClickListener,
    private val channelDeleteListener: ChannelListView.ChannelClickListener,
    private val channelMoreOptionsListener: ChannelListView.ChannelClickListener,
    private val userClickListener: ChannelListView.UserClickListener,
    private val swipeListener: ChannelListView.SwipeListener,
    private val style: ChannelListViewStyle,
    private val binding: StreamUiChannelListItemViewBinding = StreamUiChannelListItemViewBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
    private val globalState: GlobalState = ChatClient.instance().globalState,
) : SwipeViewHolder(binding.root) {
    private val currentUser = globalState.user

    private var optionsCount = 1

    private val menuItemWidth = context.getDimension(R.dimen.stream_ui_channel_list_item_option_icon_width).toFloat()
    private val optionsMenuWidth
        get() = menuItemWidth * optionsCount

    private lateinit var channel: Channel

    init {
        binding.apply {
            itemBackgroundView.apply {
                moreOptionsImageView.setOnClickListener {
                    channelMoreOptionsListener.onClick(channel)
                    swipeListener.onSwipeCanceled(this@ChannelViewHolder, absoluteAdapterPosition)
                }
                deleteImageView.setOnClickListener {
                    channelDeleteListener.onClick(channel)
                }

                applyStyle(style)
            }

            itemForegroundView.apply {
                avatarView.setOnClickListener {
                    when {
                        channel.isDirectMessaging() -> currentUser.value?.let(userClickListener::onClick)
                        else -> channelClickListener.onClick(channel)
                    }
                }
                root.apply {
                    setOnClickListener {
                        if (!swiping) {
                            channelClickListener.onClick(channel)
                        }
                    }
                    setOnLongClickListener {
                        if (!swiping) {
                            channelLongClickListener.onLongClick(channel)
                        } else true // consume if we're swiping
                    }
                    doOnNextLayout {
                        setSwipeListener(root, swipeListener)
                    }
                }

                applyStyle(style)
            }
        }
    }

    override fun bind(channel: Channel, diff: ChannelListPayloadDiff) {
        this.channel = channel

        configureForeground(diff, channel)
        configureBackground()

        listener?.onRestoreSwipePosition(this, absoluteAdapterPosition)
    }

    override fun getSwipeView(): View {
        return binding.itemForegroundView.root
    }

    /**
     * The position whe the swipe view is swiped
     */
    override fun getOpenedX(): Float {
        val isRtl = context.isRtlLayout

        return if (isRtl) optionsMenuWidth else -optionsMenuWidth
    }

    /**
     * The default position of swipe view
     */
    override fun getClosedX(): Float {
        return 0f
    }

    /**
     * Whether the swipe view is swiped or not.
     */
    override fun isSwiped(): Boolean {
        val swipeLimit = getOpenedX().absoluteValue / 2
        val swipe = getSwipeView().x.absoluteValue

        return swipe >= swipeLimit
    }

    /**
     * The range of the swipe
     */
    override fun getSwipeDeltaRange(): ClosedFloatingPointRange<Float> {
        val isRtl = context.isRtlLayout

        return if (isRtl) {
            getClosedX()..getOpenedX()
        } else {
            getOpenedX()..getClosedX()
        }
    }

    override fun isSwipeEnabled(): Boolean {
        return optionsCount > 0 && style.swipeEnabled
    }

    private fun configureBackground() {
        configureBackgroundButtons()
    }

    private fun configureBackgroundButtons() {
        var optionsCount = 0

        binding.itemBackgroundView.moreOptionsImageView.apply {
            if (style.optionsEnabled) {
                isVisible = true
                optionsCount++
            } else {
                isVisible = false
            }
        }
        binding.itemBackgroundView.deleteImageView.apply {
            val canDeleteChannel = channel.ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)
            if (canDeleteChannel && style.deleteEnabled) {
                isVisible = true
                optionsCount++
            } else {
                isVisible = false
            }
        }

        this.optionsCount = optionsCount
    }

    private fun configureForeground(diff: ChannelListPayloadDiff, channel: Channel) {
        binding.itemForegroundView.apply {
            diff.run {
                if (nameChanged || (channel.isAnonymousChannel() && diff.usersChanged)) {
                    configureChannelNameLabel()
                }

                if (avatarViewChanged) {
                    configureAvatarView()
                }

                val lastMessage = channel.getLastMessage()
                if (lastMessageChanged) {
                    configureLastMessageLabelAndTimestamp(lastMessage)
                }

                if (readStateChanged || lastMessageChanged) {
                    configureCurrentUserLastMessageStatus(lastMessage)
                }

                if (unreadCountChanged) {
                    configureUnreadCountBadge()
                }

                muteIcon.isVisible = channel.isMuted
            }
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureChannelNameLabel() {
        channelNameLabel.text = ChatUI.channelNameFormatter.formatChannelName(
            channel = channel,
            currentUser = ChatClient.instance().getCurrentUser()
        )
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureAvatarView() {
        avatarView.setChannelData(channel)
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureLastMessageLabelAndTimestamp(
        lastMessage: Message?,
    ) {
        lastMessageLabel.isVisible = lastMessage.isNotNull()
        lastMessageTimeLabel.isVisible = lastMessage.isNotNull()

        lastMessage ?: return

        lastMessageLabel.text = channel.getLastMessagePreviewText(context, channel.isDirectMessaging())
        lastMessageTimeLabel.text = ChatUI.dateFormatter.formatDate(lastMessage.getCreatedAtOrThrow())
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureUnreadCountBadge() {
        val count = channel.unreadCount ?: 0

        val haveUnreadMessages = count > 0
        unreadCountBadge.isVisible = haveUnreadMessages

        if (!haveUnreadMessages) {
            return
        }

        unreadCountBadge.text = if (count > 99) {
            "99+"
        } else {
            count.toString()
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureCurrentUserLastMessageStatus(
        lastMessage: Message?,
    ) {
        messageStatusImageView.isVisible = lastMessage != null && style.showChannelDeliveryStatusIndicator

        if (lastMessage == null || !style.showChannelDeliveryStatusIndicator) return

        // read - if the last message doesn't belong to current user, or if channel reads indicates it
        // delivered - if the last message belongs to the current user and reads indicate it wasn't read
        // pending - if the sync status says it's pending

        val currentUserSentLastMessage = lastMessage.user.id == globalState.user.value?.id
        if (!currentUserSentLastMessage) {
            messageStatusImageView.setImageDrawable(null)
            return
        }

        val messageRequiresSync = lastMessage.syncStatus in setOf(
            SyncStatus.IN_PROGRESS,
            SyncStatus.SYNC_NEEDED,
            SyncStatus.AWAITING_ATTACHMENTS
        )

        val messageStatusIndicatorIcon = if (messageRequiresSync) {
            style.indicatorPendingSyncIcon
        } else {
            val lastMessageWasRead = channel.isMessageRead(lastMessage)

            if (lastMessageWasRead) style.indicatorReadIcon else style.indicatorSentIcon
        }

        messageStatusImageView.setImageDrawable(messageStatusIndicatorIcon)
    }

    private fun StreamUiChannelListItemBackgroundViewBinding.applyStyle(style: ChannelListViewStyle) {
        root.setBackgroundColor(style.backgroundLayoutColor)
        deleteImageView.setImageDrawable(style.deleteIcon)
        moreOptionsImageView.setImageDrawable(style.optionsIcon)
    }

    private fun StreamUiChannelListItemForegroundViewBinding.applyStyle(style: ChannelListViewStyle) {
        root.backgroundTintList = ColorStateList.valueOf(style.foregroundLayoutColor)
        style.channelTitleText.apply(channelNameLabel)
        style.lastMessageText.apply(lastMessageLabel)
        style.lastMessageDateText.apply(lastMessageTimeLabel)
        style.unreadMessageCounterText.apply(unreadCountBadge)
        unreadCountBadge.backgroundTintList = ColorStateList.valueOf(style.unreadMessageCounterBackgroundColor)
        muteIcon.setImageDrawable(style.mutedChannelIcon)
    }
}
