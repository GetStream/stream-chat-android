package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.View
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.databinding.StreamChannelListItemViewBinding
import io.getstream.chat.android.ui.utils.extensions.EMPTY
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getCurrentUser
import io.getstream.chat.android.ui.utils.extensions.getCurrentUserUnreadCount
import io.getstream.chat.android.ui.utils.extensions.getLastMessage
import io.getstream.chat.android.ui.utils.extensions.getLastMessageTime
import io.getstream.chat.android.ui.utils.extensions.getPreviewText
import io.getstream.chat.android.ui.utils.extensions.getReadStatusDrawable
import io.getstream.chat.android.ui.utils.extensions.getSenderDisplayName
import io.getstream.chat.android.ui.utils.extensions.getUsers
import java.text.SimpleDateFormat
import java.util.Locale

public class ChannelListItemViewHolder(itemView: View) : BaseChannelListItemViewHolder(itemView) {

    public companion object {
        @SuppressLint("ConstantLocale")
        private val DEFAULT_LOCALE: Locale = Locale.getDefault()

        private val TIME_FORMAT = SimpleDateFormat("hh:mm a", DEFAULT_LOCALE)
    }

    public override fun bind(
        channel: Channel,
        diff: ChannelDiff?,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener,
        userClickListener: ChannelListView.UserClickListener,
        style: ChannelListViewStyle?
    ) {
        StreamChannelListItemViewBinding.bind(itemView).apply {
            diff?.run {
                if (nameChanged) {
                    configureChannelNameLabel(channel)
                }

                if (avatarViewChanged) {
                    configureAvatarView(channel, userClickListener, channelClickListener)
                }

                if (lastMessageChanged) {
                    configureLastMessageLabel(channel)
                    configureLastMessageTimestamp(channel)
                    configureUnreadCountBadge(channel)
                }

                if (readStateChanged) {
                    configureReadStateImage(channel)
                }

                configureClickListeners(channel, channelClickListener, channelLongClickListener)
            }

            style?.let { applyStyle(this, it) }
        }
    }

    private fun StreamChannelListItemViewBinding.configureClickListeners(
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

    private fun StreamChannelListItemViewBinding.configureReadStateImage(channel: Channel) {
        messageStatusImageView.setImageDrawable(channel.getReadStatusDrawable(context))
    }

    private fun StreamChannelListItemViewBinding.configureChannelNameLabel(channel: Channel) {
        channelNameLabel.text = channel.getSenderDisplayName()
    }

    private fun StreamChannelListItemViewBinding.configureAvatarView(
        channel: Channel,
        userClickListener: ChannelListView.UserClickListener,
        channelClickListener: ChannelListView.ChannelClickListener
    ) {
        avatarView.setChannelData(channel, channel.getUsers())
        avatarView.setOnClickListener {
            when (channel.getUsers().size) {
                1 -> userClickListener.onUserClick(channel.getCurrentUser())
                else -> channelClickListener.onClick(channel)
            }
        }
    }

    private fun StreamChannelListItemViewBinding.configureUnreadCountBadge(channel: Channel) {
        channel.getCurrentUserUnreadCount().let { unreadCount ->
            unreadCountBadge.apply {
                isVisible = unreadCount > 0
                text = unreadCount.toString()
            }
        }
    }

    private fun StreamChannelListItemViewBinding.configureLastMessageTimestamp(channel: Channel) {
        channel.getLastMessageTime()
            ?.let(TIME_FORMAT::format)
            ?.let { lastMessageTimeLabel.text = it }
    }

    private fun StreamChannelListItemViewBinding.configureLastMessageLabel(channel: Channel) {
        lastMessageLabel.text = channel.getLastMessage()?.getPreviewText(context) ?: String.EMPTY
    }

    private var styleApplied = false
    private fun applyStyle(binding: StreamChannelListItemViewBinding, style: ChannelListViewStyle) {
        if (styleApplied) {
            return
        }

        binding.apply {
            val px = TypedValue.COMPLEX_UNIT_PX
            channelNameLabel.setTextSize(px, style.channelTitleText.size.toFloat())
            lastMessageLabel.setTextSize(px, style.lastMessage.size.toFloat())
            lastMessageTimeLabel.setTextSize(px, style.lastMessageDateText.size.toFloat())
        }

        styleApplied = true
    }
}
