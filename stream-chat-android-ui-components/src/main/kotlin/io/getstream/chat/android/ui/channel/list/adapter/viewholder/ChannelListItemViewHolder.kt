package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.View
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelItemDiff
import io.getstream.chat.android.ui.databinding.StreamChannelListItemViewBinding
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getCurrentUser
import io.getstream.chat.android.ui.utils.extensions.getCurrentUserRead
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.android.ui.utils.extensions.getLastMessageDisplayText
import io.getstream.chat.android.ui.utils.extensions.getLastMessageTime
import io.getstream.chat.android.ui.utils.extensions.getReadStatusDrawable
import io.getstream.chat.android.ui.utils.extensions.getUsers
import java.text.SimpleDateFormat
import java.util.Locale

public class ChannelListItemViewHolder(
    itemView: View,
    public var channelClickListener: ChannelListView.ChannelClickListener? = null,
    public var channelLongClickListener: ChannelListView.ChannelClickListener? = null,
    public var userClickListener: ChannelListView.UserClickListener? = null,
    public var style: ChannelListViewStyle? = null
) : BaseChannelListItemViewHolder(itemView) {

    public companion object {
        @SuppressLint("ConstantLocale")
        private val DEFAULT_LOCALE: Locale = Locale.getDefault()

        private val TIME_FORMAT = SimpleDateFormat("hh:mm", DEFAULT_LOCALE)
    }

    public override fun bind(channel: Channel, position: Int, diff: ChannelItemDiff?) {
        StreamChannelListItemViewBinding.bind(itemView).apply {
            diff?.run {
                if (nameChanged) {
                    channelNameLabel.text = channel.getDisplayName()
                }

                if (avatarViewChanged) {
                    avatarView.setChannelData(channel, channel.getUsers())
                    avatarView.setOnClickListener {
                        when (channel.getUsers().size) {
                            1 -> userClickListener?.onUserClick(channel.getCurrentUser())
                            else -> channelClickListener?.onClick(channel)
                        }
                    }
                }

                if (lastMessageChanged) {
                    lastMessageLabel.text = channel.getLastMessageDisplayText()
                    channel.getLastMessageTime()
                        ?.let(TIME_FORMAT::format)
                        ?.let { lastMessageTimeLabel.text = it }

                    channel.getCurrentUserRead()?.unreadMessages?.let { unreadCount ->
//                        BadgeDrawable.create(context).apply {
//                            number = unreadCount
//                            badgeTextColor = ContextCompat.getColor(context, android.R.color.white)
//                            backgroundColor = ContextCompat.getColor(context, android.R.color.holo_red_dark)
//                        }
                    }
                }

                if (readStateChanged) {
                    messageStatusImageView.setImageDrawable(channel.getReadStatusDrawable(context))
                }

                root.setOnClickListener {
                    channelClickListener?.onClick(channel)
                }

                root.setOnLongClickListener {
                    channelLongClickListener?.onClick(channel)
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
