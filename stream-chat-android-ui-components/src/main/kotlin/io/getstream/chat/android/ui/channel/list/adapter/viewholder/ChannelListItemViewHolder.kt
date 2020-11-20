package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.util.TypedValue
import android.view.View
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.databinding.StreamChannelListItemViewBinding
import io.getstream.chat.android.ui.utils.extensions.EMPTY
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getCurrentUser
import io.getstream.chat.android.ui.utils.extensions.getCurrentUserUnreadCount
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.android.ui.utils.extensions.getLastMessage
import io.getstream.chat.android.ui.utils.extensions.getLastMessageTime
import io.getstream.chat.android.ui.utils.extensions.getReadStatusDrawable
import io.getstream.chat.android.ui.utils.extensions.getUsers

public class ChannelListItemViewHolder(itemView: View) : BaseChannelListItemViewHolder(itemView) {

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
                    channelNameLabel.text = channel.getDisplayName()
                }

                if (avatarViewChanged) {
                    avatarView.setChannelData(channel, channel.getUsers())
                    avatarView.setOnClickListener {
                        when (channel.getUsers().size) {
                            1 -> userClickListener.onUserClick(channel.getCurrentUser())
                            else -> channelClickListener.onClick(channel)
                        }
                    }
                }

                if (lastMessageChanged) {
                    lastMessageLabel.text = channel.getLastMessage()?.let { lastMessage ->
                        context.getString(
                            R.string.stream_channel_item_last_message_template,
                            lastMessage.getDisplayName(),
                            lastMessage.text
                        )
                    } ?: String.EMPTY

                    lastMessageTimeLabel.text = DateFormatter.formatAsTimeOrDate(channel.getLastMessageTime())

                    channel.getCurrentUserUnreadCount().let { unreadCount ->
                        unreadCountBadge.apply {
                            isVisible = unreadCount > 0
                            text = unreadCount.toString()
                        }
                    }
                }

                if (readStateChanged) {
                    messageStatusImageView.setImageDrawable(channel.getReadStatusDrawable(context))
                }

                root.setOnClickListener {
                    channelClickListener.onClick(channel)
                }

                root.setOnLongClickListener {
                    channelLongClickListener.onClick(channel)
                    true
                }
            }

            style?.let { applyStyle(this, it) }
        }
    }

    private fun applyStyle(binding: StreamChannelListItemViewBinding, style: ChannelListViewStyle) {
        binding.apply {
            val px = TypedValue.COMPLEX_UNIT_PX
            channelNameLabel.setTextSize(px, style.channelTitleText.size.toFloat())
            lastMessageLabel.setTextSize(px, style.lastMessage.size.toFloat())
            lastMessageTimeLabel.setTextSize(px, style.lastMessageDateText.size.toFloat())
        }
    }
}
