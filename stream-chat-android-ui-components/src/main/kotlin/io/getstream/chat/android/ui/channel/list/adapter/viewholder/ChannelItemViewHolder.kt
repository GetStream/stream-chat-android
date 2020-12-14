package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.DateFormatter
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
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.android.ui.utils.extensions.getLastMessage
import io.getstream.chat.android.ui.utils.extensions.getLastMessagePreviewText
import io.getstream.chat.android.ui.utils.extensions.inflater
import io.getstream.chat.android.ui.utils.extensions.isDirectMessaging
import io.getstream.chat.android.ui.utils.extensions.isMessageRead
import io.getstream.chat.android.ui.utils.extensions.isNotNull
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx
import kotlin.math.absoluteValue

public class ChannelItemViewHolder @JvmOverloads constructor(
    parent: ViewGroup,
    private val channelClickListener: ChannelListView.ChannelClickListener,
    private val channelLongClickListener: ChannelListView.ChannelClickListener,
    private val channelDeleteListener: ChannelListView.ChannelClickListener,
    private val channelMoreOptionsListener: ChannelListView.ChannelClickListener,
    private val userClickListener: ChannelListView.UserClickListener,
    private val swipeDelegate: ChannelListView.ViewHolderSwipeDelegate,
    private val style: ChannelListViewStyle?,
    private val binding: StreamUiChannelListItemViewBinding = StreamUiChannelListItemViewBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseChannelListItemViewHolder(binding.root) {

    private val dateFormatter = DateFormatter.from(context)
    private val currentUser = ChatDomain.instance().currentUser

    public companion object {
        public const val OPTIONS_COUNT: Int = 2

        // persists menu states for channels - becomes necessary when view holders are recycled
    }

    public override fun bind(channel: Channel, diff: ChannelDiff) {
        configureForeground(diff, channel)
        configureBackground(channel)
    }

    public fun getItemViewForeground(): View = binding.itemForegroundView.root

    public fun getItemViewBackground(): View = binding.itemBackgroundView.root

    private fun configureBackground(channel: Channel) {
        binding.itemBackgroundView.apply {
            moreOptionsImageView.setOnClickListener {
                channelMoreOptionsListener.onClick(channel)
            }
            deleteImageView.setOnClickListener {
                channelDeleteListener.onClick(channel)
            }
        }
    }

    private fun configureForeground(diff: ChannelDiff, channel: Channel) {
        binding.itemForegroundView.apply {
            configureSwipeBehavior(channel.cid)

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

            style?.let { applyStyle(this, it) }
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
        channelNameLabel.text = channel.getDisplayName()
    }

    private fun StreamUiChannelListItemForegroundViewBinding.configureAvatarView(
        channel: Channel,
        userClickListener: ChannelListView.UserClickListener,
        channelClickListener: ChannelListView.ChannelClickListener
    ) {
        avatarView.setChannelData(channel)
        avatarView.setOnClickListener {
            when {
                channel.isDirectMessaging() -> userClickListener.onUserClick(currentUser)
                else -> channelClickListener.onClick(channel)
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

    private fun applyStyle(binding: StreamUiChannelListItemForegroundViewBinding, style: ChannelListViewStyle) {
        if (styleApplied) {
            return
        }

        binding.apply {
            channelNameLabel.setTextSizePx(style.channelTitleText.size.toFloat())
            lastMessageLabel.setTextSizePx(style.lastMessage.size.toFloat())
            lastMessageTimeLabel.setTextSizePx(style.lastMessageDateText.size.toFloat())
        }
    }

    public val multiSwipeEnabled: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    private fun StreamUiChannelListItemForegroundViewBinding.configureSwipeBehavior(cid: String) {
        var startX = 0f
        var startY = 0f
        var prevX = 0f
        var swiping = false
        var wasSwiping = false
        val position = absoluteAdapterPosition

        // restore the view's last state
        swipeDelegate.onRestoreSwipePosition(this@ChannelItemViewHolder, absoluteAdapterPosition)

        root.setOnTouchListener { _, event ->

            val rawX = event.rawX
            val rawY = event.rawY

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    // store the starting x & y so we can calculate total deltas
                    startX = rawX
                    startY = rawY
                    // initialize the previous x to the start values
                    prevX = startX
                    // don't know if it's a swipe yet; assume it's not
                    swiping = false
                    // don't consume
                    swiping
                }

                MotionEvent.ACTION_MOVE -> {
                    // calculate the total delta for both axes
                    val totalDeltaX = rawX - startX
                    val totalDeltaY = rawY - startY
                    // calculate the delta from the last event to this one
                    val lastMoveDeltaX = rawX - prevX
                    // now that we've calculated, update the previous x value with this event's x
                    prevX = rawX
                    // store the old swiping value so we can determine if we were ever swiping
                    wasSwiping = swiping
                    // determine if it's a swipe by comparing total axis delta magnitude
                    swiping = totalDeltaX.absoluteValue > totalDeltaY.absoluteValue

                    when {
                        // we've started swiping
                        !wasSwiping && swiping -> {
                            swipeDelegate.onSwipeStarted(this@ChannelItemViewHolder, position, rawX, rawY)
                        }
                        // signal swipe movement
                        swiping -> {
                            swipeDelegate.onSwipeChanged(this@ChannelItemViewHolder, position, lastMoveDeltaX)
                        }
                        // axis magnitude measurement has dictated we are no longer swiping
                        wasSwiping && !swiping -> {
                            swipeDelegate.onSwipeEnded(this@ChannelItemViewHolder, position, rawX, rawY)
                        }
                    }
                    // consume if we are swiping
                    swiping
                }

                MotionEvent.ACTION_UP -> {
                    // signal end of swipe
                    swipeDelegate.onSwipeEnded(this@ChannelItemViewHolder, position, rawX, rawY)
                    wasSwiping = false
                    // consume if we were swiping
                    swiping
                }

                MotionEvent.ACTION_CANCEL -> {
                    // take action if we were swiping, otherwise leave it alone
                    if (wasSwiping) {
                        // no longer swiping...
                        swiping = false
                        wasSwiping = false
                        // signal cancellation
                        swipeDelegate.onSwipeCanceled(this@ChannelItemViewHolder, position, rawX, rawY)
                    }

                    wasSwiping
                }

                else -> false
            }
        }
    }
}
