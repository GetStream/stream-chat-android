package io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal

import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import com.getstream.sdk.chat.utils.formatDate
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
import io.getstream.chat.android.ui.common.extensions.internal.context
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getLastMessage
import io.getstream.chat.android.ui.common.extensions.internal.getLastMessagePreviewText
import io.getstream.chat.android.ui.common.extensions.internal.isMessageRead
import io.getstream.chat.android.ui.common.extensions.internal.isNotNull
import io.getstream.chat.android.ui.common.extensions.internal.setTextSizePx
import io.getstream.chat.android.ui.common.extensions.isCurrentUserOwnerOrAdmin
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
        parent.inflater,
        parent,
        false
    ),
) : SwipeViewHolder(binding.root) {
    private val dateFormatter = DateFormatter.from(context)
    private val currentUser = ChatDomain.instance().currentUser

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
            }

            itemForegroundView.apply {
                avatarView.setOnClickListener {
                    when {
                        channel.isDirectMessaging() -> userClickListener.onClick(currentUser)
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

        configureForeground(diff)
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

    private fun configureBackground() {
        configureDeleteButton()
    }

    private fun configureDeleteButton() {
        val canDeleteChannel = channel.members.isCurrentUserOwnerOrAdmin()
        binding.itemBackgroundView.deleteImageView.apply {
            if (canDeleteChannel) {
                optionsCount = 2
                isVisible = true
            } else {
                optionsCount = 1
                isVisible = false
            }
        }
    }

    private fun configureForeground(diff: ChannelListPayloadDiff) {
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

        lastMessageLabel.text = channel.getLastMessagePreviewText(context, channel.isDirectMessaging())
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

        val currentUserSentLastMessage = lastMessage.user.id == ChatDomain.instance().currentUser.id
        val lastMessageByCurrentUserWasRead = channel.isMessageRead(lastMessage)
        when {
            !currentUserSentLastMessage || lastMessageByCurrentUserWasRead -> {
                messageStatusImageView.setImageResource(R.drawable.stream_ui_ic_check_double)
            }

            currentUserSentLastMessage && !lastMessageByCurrentUserWasRead -> {
                messageStatusImageView.setImageResource(R.drawable.stream_ui_ic_check_single)
            }

            else -> determineLastMessageSyncStatus(lastMessage)
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.determineLastMessageSyncStatus(message: Message) {
        when (message.syncStatus) {
            SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED -> {
                messageStatusImageView.setImageResource(R.drawable.stream_ui_ic_clock)
            }

            SyncStatus.COMPLETED -> {
                messageStatusImageView.setImageResource(R.drawable.stream_ui_ic_check_single)
            }

            SyncStatus.FAILED_PERMANENTLY -> {
                // no direction on this yet
            }
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.applyStyle(style: ChannelListViewStyle) {
        binding.apply {
            channelNameLabel.setTextSizePx(style.channelTitleTextSize)
            lastMessageLabel.setTextSizePx(style.lastMessageSize)
            lastMessageTimeLabel.setTextSizePx(style.lastMessageDateTextSize)
        }
    }
}
