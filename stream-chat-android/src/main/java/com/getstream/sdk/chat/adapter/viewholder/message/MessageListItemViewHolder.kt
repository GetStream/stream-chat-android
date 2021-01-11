package com.getstream.sdk.chat.adapter.viewholder.message

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.AttachmentViewHolderFactory
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.AttachmentConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.IndicatorConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.MarginConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.MessageTextConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.ReactionConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.ReplyConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.SpaceConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.UserAvatarConfigurator
import com.getstream.sdk.chat.adapter.viewholder.message.configurators.UsernameAndDateConfigurator
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

public open class MessageListItemViewHolder(
    parent: ViewGroup,
    style: MessageListViewStyle,
    channel: Channel,
    viewHolderFactory: AttachmentViewHolderFactory,
    bubbleHelper: BubbleHelper,
    dateFormatter: DateFormatter,
    messageClickListener: MessageClickListener,
    messageLongClickListener: MessageLongClickListener,
    messageRetryListener: MessageListView.MessageRetryListener,
    reactionViewClickListener: ReactionViewClickListener,
    userClickListener: MessageListView.UserClickListener,
    readStateClickListener: ReadStateClickListener,
    binding: StreamItemMessageBinding =
        StreamItemMessageBinding.inflate(parent.inflater, parent, false)
) : BaseMessageListItemViewHolder<MessageItem>(binding.root) {

    private val marginConfigurator = MarginConfigurator(binding, style)
    private val spaceConfigurator = SpaceConfigurator(binding)
    private val usernameAndDateConfigurator = UsernameAndDateConfigurator(binding, style, dateFormatter)
    private val messageTextConfigurator = MessageTextConfigurator(
        binding,
        context,
        style,
        bubbleHelper,
        messageClickListener,
        messageLongClickListener,
        messageRetryListener
    )
    private val attachmentConfigurator = AttachmentConfigurator(
        binding,
        style,
        viewHolderFactory
    )
    private val indicatorConfigurator = IndicatorConfigurator(
        binding,
        style.readStateStyle,
        readStateClickListener
    )
    private val reactionConfigurator = ReactionConfigurator(
        binding,
        context,
        style,
        channel,
        reactionViewClickListener,
        configParamsReadIndicator = { messageItem ->
            indicatorConfigurator.configParamsReadIndicator(messageItem)
        }
    )
    private val replyConfigurator = ReplyConfigurator(
        binding,
        context,
        style,
        channel,
        messageClickListener,
        bindingAdapterPosition = { bindingAdapterPosition }
    )
    private val userAvatarConfigurator = UserAvatarConfigurator(
        binding,
        context,
        style,
        userClickListener
    )

    override fun bind(messageListItem: MessageItem, diff: MessageListItemPayloadDiff) {
        listOfNotNull(
            spaceConfigurator,
            marginConfigurator,
            messageTextConfigurator.takeIf { diff.text || diff.positions || diff.deleted || diff.reactions || diff.syncStatus },
            attachmentConfigurator.takeIf { diff.attachments },
            reactionConfigurator.takeIf { diff.reactions },
            replyConfigurator.takeIf { diff.replies },
            indicatorConfigurator.takeIf { diff.syncStatus || diff.readBy },
            userAvatarConfigurator.takeIf { diff.positions },
            usernameAndDateConfigurator.takeIf { diff.positions }
        ).forEach { it.configure(messageListItem) }
    }
}
