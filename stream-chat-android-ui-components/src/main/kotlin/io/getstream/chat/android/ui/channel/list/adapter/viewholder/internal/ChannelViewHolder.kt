package io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import com.getstream.sdk.chat.utils.formatDate
import io.getstream.chat.android.client.extensions.isMuted
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.SwipeViewHolder
import io.getstream.chat.android.ui.common.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.common.extensions.getDisplayName
import io.getstream.chat.android.ui.common.extensions.internal.asMention
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getLastMessage
import io.getstream.chat.android.ui.common.extensions.internal.getLastMessagePreviewText
import io.getstream.chat.android.ui.common.extensions.internal.isMessageRead
import io.getstream.chat.android.ui.common.extensions.internal.isNotNull
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.isCurrentUserOwnerOrAdmin
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemBackgroundViewBinding
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemForegroundViewBinding
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemViewBinding

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
) : SwipeViewHolder(binding.root) {
    private val dateFormatter = DateFormatter.from(context)

    private val logger = ChatLogger.get("ChannelViewHolder")

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
                        channel.isDirectMessaging() -> {
                            ChatDomain.instance().user.value?.let(userClickListener::onClick) ?: run {
                                logger.logE("User click can't be handled because user it not set for ChatDomain")
                            }
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

    override fun getOpenedX(): Float {
        return -optionsMenuWidth
    }

    override fun getClosedX(): Float {
        return 0f
    }

    override fun getSwipeDeltaRange(): ClosedFloatingPointRange<Float> {
        val openedX = getOpenedX()
        val closedX = getClosedX()
        return openedX.coerceAtMost(closedX)..openedX.coerceAtLeast(closedX)
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
            val canDeleteChannel = channel.members.isCurrentUserOwnerOrAdmin()
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
                if (nameChanged) {
                    configureChannelNameLabel()
                }

                if (avatarViewChanged) {
                    configureAvatarView()
                }

                val lastMessage = channel.getLastMessage()
                if (lastMessageChanged) {
                    configureLastMessageLabelAndTimestamp(lastMessage)
                }

                if (readStateChanged) {
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
        channelNameLabel.text = channel.getDisplayName(context)
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

        ChatDomain.instance().user.value?.let { user ->
            lastMessageLabel.text =
                channel.getLastMessagePreviewText(context, channel.isDirectMessaging(), user.asMention(context))
        } ?: logger.logE("User is not set in ChatDomain. The text of last message can not be set.")

        lastMessageTimeLabel.text = dateFormatter.formatDate(lastMessage.getCreatedAtOrThrow())
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
        messageStatusImageView.isVisible = lastMessage != null

        lastMessage ?: return

        // read - if the last message doesn't belong to current user, or if channel reads indicates it
        // delivered - if the last message belongs to the current user and reads indicate it wasn't read
        // pending - if the sync status says it's pending

        val currentUserSentLastMessage = lastMessage.user.id == ChatDomain.instance().user.value?.id
        val lastMessageByCurrentUserWasRead = channel.isMessageRead(lastMessage)
        when {
            !currentUserSentLastMessage || lastMessageByCurrentUserWasRead -> {
                messageStatusImageView.setImageDrawable(style.indicatorReadIcon)
            }

            currentUserSentLastMessage && !lastMessageByCurrentUserWasRead -> {
                messageStatusImageView.setImageDrawable(style.indicatorSentIcon)
            }

            else -> determineLastMessageSyncStatus(lastMessage)
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.determineLastMessageSyncStatus(message: Message) {
        when (message.syncStatus) {
            SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED -> {
                messageStatusImageView.setImageDrawable(style.indicatorPendingSyncIcon)
            }

            SyncStatus.COMPLETED -> {
                messageStatusImageView.setImageDrawable(style.indicatorSentIcon)
            }

            SyncStatus.FAILED_PERMANENTLY -> {
                // no direction on this yet
            }
        }
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
        muteIcon.setImageDrawable(
            style.mutedChannelIcon.apply { setTint(style.mutedChannelIconTint) }
        )
    }
}
