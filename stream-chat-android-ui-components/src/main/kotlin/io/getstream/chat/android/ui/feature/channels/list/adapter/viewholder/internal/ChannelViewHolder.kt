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

package io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.internal

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.client.extensions.isAnonymousChannel
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.common.extensions.internal.isNotNull
import io.getstream.chat.android.ui.common.utils.extensions.isDirectMessaging
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemBackgroundViewBinding
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemForegroundViewBinding
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemViewBinding
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.channels.list.ChannelListViewStyle
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.SwipeViewHolder
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getLastMessage
import io.getstream.chat.android.ui.utils.extensions.isMuted
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout
import io.getstream.chat.android.ui.utils.extensions.readCount
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import kotlin.math.abs
import kotlin.math.absoluteValue

@Suppress("LongParameterList")
internal class ChannelViewHolder @JvmOverloads constructor(
    parent: ViewGroup,
    private val channelClickListener: ChannelListView.ChannelClickListener,
    private val channelLongClickListener: ChannelListView.ChannelLongClickListener,
    private val channelDeleteListener: ChannelListView.ChannelClickListener,
    private val channelMoreOptionsListener: ChannelListView.ChannelClickListener,
    private val userClickListener: ChannelListView.UserClickListener,
    private val swipeListener: ChannelListView.SwipeListener,
    private val style: ChannelListViewStyle,
    private val isMoreOptionsVisible: ChannelListView.ChannelOptionVisibilityPredicate,
    private val isDeleteOptionVisible: ChannelListView.ChannelOptionVisibilityPredicate,
    private val getMoreOptionsIcon: ChannelListView.ChannelOptionIconProvider,
    private val getDeleteOptionIcon: ChannelListView.ChannelOptionIconProvider,
    private val binding: StreamUiChannelListItemViewBinding = StreamUiChannelListItemViewBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false,
    ),
) : SwipeViewHolder(binding.root) {
    private val currentUser = ChatUI.currentUserProvider.getCurrentUser()

    private var optionsCount = 1

    private val menuItemWidth = context.getDimension(R.dimen.stream_ui_channel_list_item_option_icon_width).toFloat()
    private val optionsMenuWidth
        get() = menuItemWidth * optionsCount

    private lateinit var channel: Channel

    init {
        binding.apply {
            channelItemView.updateLayoutParams {
                height = style.itemHeight
            }
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
                channelAvatarView.setOnClickListener {
                    when {
                        channel.isDirectMessaging() -> {
                            channel.members.filterNot { currentUser?.id == it.user.id }
                                .firstOrNull()
                                ?.user
                                ?.let(userClickListener::onClick)
                        }
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
                        } else {
                            true // consume if we're swiping
                        }
                    }
                    doOnNextLayout {
                        setSwipeListener(root, swipeListener)
                    }
                }

                applyStyle(style)
            }
        }
    }

    override fun bind(channelItem: ChannelListItem.ChannelItem, diff: ChannelListPayloadDiff) {
        this.channel = channelItem.channel

        configureForeground(diff, channelItem)
        configureBackground()

        listener?.onRestoreSwipePosition(this, absoluteAdapterPosition)
    }

    override fun getSwipeView(): View = binding.itemForegroundView.root

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
    override fun getClosedX(): Float = 0f

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

    override fun isSwipeEnabled(): Boolean = optionsCount > 0 && style.swipeEnabled

    private fun configureBackground() {
        configureBackgroundButtons()
    }

    private fun configureBackgroundButtons() {
        var optionsCount = 0

        binding.itemBackgroundView.moreOptionsImageView.apply {
            if (style.optionsEnabled && isMoreOptionsVisible(channel)) {
                isVisible = true
                getMoreOptionsIcon.invoke(channel)?.also { setImageDrawable(it) }
                optionsCount++
            } else {
                isVisible = false
            }
        }
        binding.itemBackgroundView.deleteImageView.apply {
            if (style.deleteEnabled && isDeleteOptionVisible(channel)) {
                isVisible = true
                getDeleteOptionIcon.invoke(channel)?.also { setImageDrawable(it) }
                optionsCount++
            } else {
                isVisible = false
            }
        }

        this.optionsCount = optionsCount
    }

    private fun configureForeground(diff: ChannelListPayloadDiff, channelItem: ChannelListItem.ChannelItem) {
        binding.itemForegroundView.apply {
            diff.run {
                val lastMessage = channelItem.channel.getLastMessage()
                if (channelNameLabelChanged(channelItem.channel.isAnonymousChannel())) {
                    configureChannelNameLabel(lastMessage, channelItem)
                }

                if (avatarViewChanged) {
                    configureAvatarView()
                }
                if (lastMessageChanged || typingUsersChanged || draftMessageChanged) {
                    lastMessageLabel.isVisible =
                        channelItem.typingUsers.isEmpty()
                            .and(
                                lastMessage.isNotNull()
                                    .or(channelItem.draftMessage.isNotNull()),
                            )
                    draftMessageLabel.isVisible = channelItem.draftMessage.isNotNull()
                        .and(channelItem.typingUsers.isEmpty())
                    configureLastMessageLabelAndTimestamp(lastMessage, channelItem.draftMessage)
                    typingIndicatorView.setTypingUsers(channelItem.typingUsers)
                }

                if (readStateChanged || lastMessageChanged) {
                    configureCurrentUserLastMessageStatus(lastMessage)
                }

                if (unreadCountChanged) {
                    configureUnreadCountBadge()
                }

                muteIcon.isVisible = channelItem.channel.isMuted
            }
        }
    }

    private fun ChannelListPayloadDiff.channelNameLabelChanged(isAnonymousChannel: Boolean): Boolean = nameChanged
        .or(typingUsersChanged)
        .or(lastMessageChanged)
        .or(usersChanged.and(isAnonymousChannel))
        .or(draftMessageChanged)

    private fun StreamUiChannelListItemForegroundViewBinding.configureChannelNameLabel(
        lastMessage: Message?,
        channelItem: ChannelListItem.ChannelItem,
    ) {
        channelNameLabel.text = ChatUI.channelNameFormatter.formatChannelName(
            channel = channel,
            currentUser = ChatUI.currentUserProvider.getCurrentUser(),
        )

        if (lastMessage != null || channelItem.typingUsers.isNotEmpty() || channelItem.draftMessage != null) {
            channelNameLabel.translationY = 0f
        } else if (channelNameLabel.height > 0) {
            channelNameLabel.translationY = yDiffBetweenCenters(channelNameLabel, foregroundView)
        } else {
            channelNameLabel.doOnPreDraw {
                channelNameLabel.translationY = yDiffBetweenCenters(channelNameLabel, foregroundView)
            }
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureAvatarView() {
        channelAvatarView.setChannel(channel)
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureLastMessageLabelAndTimestamp(
        lastMessage: Message?,
        draftMessage: DraftMessage?,
    ) {
        lastMessageTimeLabel.isVisible = lastMessage.isNotNull().and(draftMessage == null)
        lastMessageTimeLabel.text =
            lastMessage
                ?.takeUnless { draftMessage != null }
                ?.let { ChatUI.dateFormatter.formatDate(channel.lastMessageAt) }
                ?: ""
        lastMessageLabel.text =
            draftMessage?.text
                ?: lastMessage?.let {
                    ChatUI.messagePreviewFormatter.formatMessagePreview(
                        channel = channel,
                        message = it,
                        currentUser = ChatUI.currentUserProvider.getCurrentUser(),
                    )
                }
                ?: ""
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureUnreadCountBadge() {
        val count = channel.currentUserUnreadCount()

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

        val currentUserSentLastMessage = lastMessage.user.id == currentUser?.id
        if (!currentUserSentLastMessage) {
            messageStatusImageView.setImageDrawable(null)
            return
        }

        val messageRequiresSync = lastMessage.syncStatus in setOf(
            SyncStatus.IN_PROGRESS,
            SyncStatus.SYNC_NEEDED,
            SyncStatus.AWAITING_ATTACHMENTS,
        )

        val readCount = channel.readCount(lastMessage)

        val messageStatusIndicatorIcon = if (messageRequiresSync) {
            style.indicatorPendingSyncIcon
        } else {
            val lastMessageWasRead = readCount > 0

            if (lastMessageWasRead) style.indicatorReadIcon else style.indicatorSentIcon
        }

        if (readCount > 1 && style.readCountEnabled) {
            readCountView.isVisible = true
            readCountView.text = readCount.toString()
        } else {
            readCountView.isVisible = false
        }

        messageStatusImageView.setImageDrawable(messageStatusIndicatorIcon)
    }

    private fun StreamUiChannelListItemBackgroundViewBinding.applyStyle(style: ChannelListViewStyle) {
        root.setBackgroundColor(style.backgroundLayoutColor)
        backgroundView.setBackgroundColor(style.backgroundLayoutColor)
        backgroundView.updateLayoutParams {
            height = style.itemHeight
        }
        deleteImageView.setImageDrawable(style.deleteIcon)
        moreOptionsImageView.setImageDrawable(style.optionsIcon)
    }

    private fun StreamUiChannelListItemForegroundViewBinding.applyStyle(style: ChannelListViewStyle) {
        foregroundView.backgroundTintList = ColorStateList.valueOf(style.foregroundLayoutColor)
        foregroundView.updateLayoutParams {
            height = style.itemHeight
        }
        channelNameLabel.setTextStyle(style.channelTitleText)
        draftMessageLabel.setTextStyle(style.draftMessageLabel)
        lastMessageLabel.setTextStyle(style.lastMessageText)
        lastMessageTimeLabel.setTextStyle(style.lastMessageDateText)
        unreadCountBadge.setTextStyle(style.unreadMessageCounterText)
        unreadCountBadge.backgroundTintList = ColorStateList.valueOf(style.unreadMessageCounterBackgroundColor)
        muteIcon.setImageDrawable(style.mutedChannelIcon)
        channelAvatarView.updateLayoutParams<MarginLayoutParams> {
            marginStart = style.itemMarginStart
        }
        lastMessageTimeLabel.updateLayoutParams<MarginLayoutParams> {
            marginEnd = style.itemMarginEnd
        }
        unreadCountBadge.updateLayoutParams<MarginLayoutParams> {
            marginEnd = style.itemMarginEnd
        }
        channelNameLabel.updateLayoutParams<MarginLayoutParams> {
            marginStart = style.itemTitleMarginStart
        }
        spacer.updateLayoutParams {
            height = style.itemVerticalSpacerHeight
        }
        guideline.setGuidelinePercent(style.itemVerticalSpacerPosition)
    }

    private companion object {
        private fun yDiffBetweenCenters(view1: View, view2: View): Float {
            val cy1 = view1.paddingTop + view1.top + view1.height / 2f
            val cy2 = view2.paddingTop + view2.top + view2.height / 2f
            return abs(cy2 - cy1)
        }
    }
}
