package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.utils.formatDate
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemForegroundViewBinding
import io.getstream.chat.android.ui.databinding.StreamUiChannelListItemViewBinding
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.android.ui.utils.extensions.getLastMessage
import io.getstream.chat.android.ui.utils.extensions.getLastMessagePreviewText
import io.getstream.chat.android.ui.utils.extensions.isDirectMessaging
import io.getstream.chat.android.ui.utils.extensions.isMessageRead
import io.getstream.chat.android.ui.utils.extensions.isNotNull
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx

public class ChannelItemViewHolder @JvmOverloads constructor(
    parent: ViewGroup,
    private val channelClickListener: ChannelListView.ChannelClickListener,
    private val channelLongClickListener: ChannelListView.ChannelClickListener,
    private val channelDeleteListener: ChannelListView.ChannelClickListener,
    private val channelMoreOptionsListener: ChannelListView.ChannelClickListener,
    private val userClickListener: ChannelListView.UserClickListener,
    private val swipeListener: ChannelListView.SwipeListener,
    private val style: ChannelListViewStyle,
    private val binding: StreamUiChannelListItemViewBinding = StreamUiChannelListItemViewBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : SwipeViewHolder(binding.root) {

    private val dateFormatter = DateFormatter.from(context)
    private val currentUser = ChatDomain.instance().currentUser

    public companion object {
        public const val OPTIONS_COUNT: Int = 2
    }

    private val menuItemWidth = context.getDimension(R.dimen.stream_ui_channel_list_item_option_icon_width).toFloat()
    private val optionsMenuWidth = menuItemWidth * OPTIONS_COUNT

    public override fun bind(channel: Channel, diff: ChannelDiff) {
        configureForeground(diff, channel)
        configureBackground(channel)
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

    private fun configureBackground(channel: Channel) {
        binding.itemBackgroundView.apply {
            moreOptionsImageView.setOnClickListener {
                channelMoreOptionsListener.onClick(channel)
                swipeListener.onSwipeCanceled(this@ChannelItemViewHolder, absoluteAdapterPosition)
            }

            deleteImageView.setOnClickListener {
                channelDeleteListener.onClick(channel)
            }
        }
    }

    private fun configureForeground(diff: ChannelDiff, channel: Channel) {
        binding.itemForegroundView.apply {

            setSwipeListener(root, swipeListener)

            diff.run {
                if (nameChanged) {
                    configureChannelNameLabel(channel)
                }

                if (avatarViewChanged) {
                    configureAvatarView(channel, userClickListener, channelClickListener)
                }

                val lastMessage = channel.getLastMessage()
                if (lastMessageChanged) {
                    configureLastMessageLabel(channel, lastMessage)
                    configureLastMessageTimestamp(lastMessage)
                    configureUnreadCountBadge(channel)
                }

                if (readStateChanged) {
                    configureCurrentUserLastMessageStatus(channel, lastMessage)
                }
            }

            configureClickListeners(channel, channelClickListener, channelLongClickListener)

            applyStyle(style)
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureClickListeners(
        channel: Channel,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener
    ) {
        root.setOnClickListener {
            channelClickListener.onClick(channel)
        }

        root.setOnLongClickListener {
            channelLongClickListener.onClick(channel)
            true
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureChannelNameLabel(channel: Channel) {
        channelNameLabel.text = channel.getDisplayName(context)
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureAvatarView(
        channel: Channel,
        userClickListener: ChannelListView.UserClickListener,
        channelClickListener: ChannelListView.ChannelClickListener
    ) {
        avatarView.apply {
            setStyle(style.avatarStyle)
            setChannelData(channel)
            setOnClickListener {
                when {
                    channel.isDirectMessaging() -> userClickListener.onUserClick(currentUser)
                    else -> channelClickListener.onClick(channel)
                }
            }
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureUnreadCountBadge(channel: Channel) {
        val haveUnreadMessages = channel.unreadCount ?: 0 > 0
        unreadCountBadge.isVisible = haveUnreadMessages

        if (!haveUnreadMessages) {
            return
        }

        unreadCountBadge.apply {
            text = channel.unreadCount.toString()
        }
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureLastMessageTimestamp(lastMessage: Message?) {
        lastMessageTimeLabel.isVisible = lastMessage.isNotNull()

        lastMessage ?: return

        lastMessageTimeLabel.text = dateFormatter.formatDate(lastMessage.getCreatedAtOrThrow())
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureLastMessageLabel(
        channel: Channel,
        lastMessage: Message?
    ) {
        lastMessageLabel.isVisible = lastMessage.isNotNull()

        lastMessage ?: return

        lastMessageLabel.text = channel.getLastMessagePreviewText(context, channel.isDirectMessaging())
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureCurrentUserLastMessageStatus(
        channel: Channel,
        lastMessage: Message?
    ) {

        messageStatusImageView.isVisible = lastMessage != null

        lastMessage ?: return

        /**
         * read - if the last message doesn't belong to current user, or if channel reads indicates it
         * delivered - if the last message belongs to the current user and reads indicate it wasn't read
         * pending - if the sync status says it's pending
         */

        val currentUserSentLastMessage = lastMessage.user.id == ChatDomain.instance().currentUser.id
        val lastMessageByCurrentUserWasRead = channel.isMessageRead(lastMessage)
        when {
            !currentUserSentLastMessage || lastMessageByCurrentUserWasRead -> {
                messageStatusImageView.setImageResource(R.drawable.stream_ui_ic_check_all)
            }

            currentUserSentLastMessage && !lastMessageByCurrentUserWasRead -> {
                messageStatusImageView.setImageResource(R.drawable.stream_ui_ic_check_gray)
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
                messageStatusImageView.setImageResource(R.drawable.stream_ui_ic_check_gray)
            }

            SyncStatus.FAILED_PERMANENTLY -> {
                // no direction on this yet
            }
        }
    }

    private var styleApplied = false

    private fun StreamUiChannelListItemForegroundViewBinding.applyStyle(style: ChannelListViewStyle) {
        if (styleApplied) {
            return
        }

        binding.apply {
            channelNameLabel.setTextSizePx(style.channelTitleTextSize)
            lastMessageLabel.setTextSizePx(style.lastMessageSize)
            lastMessageTimeLabel.setTextSizePx(style.lastMessageDateTextSize)
        }
    }
}
