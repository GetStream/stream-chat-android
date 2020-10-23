package com.getstream.sdk.chat.adapter.viewholder.message

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.AttachmentViewHolderFactory
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.inflater
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.AttachmentConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.Configurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.IndicatorConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.MarginConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.MessageTextConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.ReactionConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.ReplyConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.SpaceConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.UserAvatarConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.UsernameAndDateConfigurator
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

internal class MessageListItemViewHolder(
    parent: ViewGroup,
    style: MessageListViewStyle,
    channel: Channel,
    viewHolderFactory: AttachmentViewHolderFactory,
    bubbleHelper: BubbleHelper,
    messageClickListener: MessageClickListener,
    messageLongClickListener: MessageLongClickListener,
    messageRetryListener: MessageListView.MessageRetryListener,
    reactionViewClickListener: ReactionViewClickListener,
    userClickListener: MessageListView.UserClickListener,
    readStateClickListener: ReadStateClickListener,
    private val binding: StreamItemMessageBinding =
        StreamItemMessageBinding.inflate(parent.inflater, parent, false)
) : BaseMessageListItemViewHolder<MessageItem>(binding.root) {

    override fun bind(messageListItem: MessageItem) {
        configurators.forEach { configurator ->
            configurator.configure(messageListItem)
        }
    }

    private val configurators: List<Configurator>

    init {
        val marginConfigurator = MarginConfigurator(
            binding,
            style
        )
        val messageTextConfigurator = MessageTextConfigurator(
            binding,
            context,
            style,
            bubbleHelper,
            messageClickListener,
            messageLongClickListener,
            messageRetryListener
        )
        val attachmentConfigurator = AttachmentConfigurator(
            binding,
            style,
            viewHolderFactory
        )
        val indicatorConfigurator = IndicatorConfigurator(
            binding,
            style,
            readStateClickListener
        )
        val reactionConfigurator = ReactionConfigurator(
            binding,
            context,
            style,
            channel,
            reactionViewClickListener,
            configParamsReadIndicator = { messageItem ->
                indicatorConfigurator.configParamsReadIndicator(messageItem)
            }
        )
        val replyConfigurator = ReplyConfigurator(
            binding,
            context,
            style,
            channel,
            messageClickListener,
            bindingAdapterPosition = { bindingAdapterPosition }
        )
        val spaceConfigurator = SpaceConfigurator(
            binding
        )
        val userAvatarConfigurator = UserAvatarConfigurator(
            binding,
            context,
            style,
            userClickListener
        )
        val usernameAndDateConfigurator = UsernameAndDateConfigurator(
            binding,
            style
        )

        configurators = listOf(
            marginConfigurator,
            messageTextConfigurator,
            attachmentConfigurator,
            reactionConfigurator,
            replyConfigurator,
            indicatorConfigurator,
            spaceConfigurator,
            userAvatarConfigurator,
            usernameAndDateConfigurator
        )
    }
}
