package com.getstream.sdk.chat.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListView.GiphySendListener
import com.getstream.sdk.chat.view.MessageListView.MessageClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import com.getstream.sdk.chat.view.MessageListView.ReactionViewClickListener
import com.getstream.sdk.chat.view.MessageListView.ReadStateClickListener
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

class MessageListItemAdapter @JvmOverloads constructor(
    private var context: Context,
    var channel: Channel? = null,
    private var messageListItemList: List<MessageListItem> = emptyList(),
    var viewHolderFactory: MessageViewHolderFactory = MessageViewHolderFactory()
) : RecyclerView.Adapter<BaseMessageListItemViewHolder<*>>() {

    var bubbleHelper: BubbleHelper? = null

    var messageClickListener: MessageClickListener? = null
        set(value) {
            if (style?.isReactionEnabled == true)
                field = value
        }
    var messageLongClickListener: MessageLongClickListener? = null
    var attachmentClickListener: AttachmentClickListener? = null
    var reactionViewClickListener: ReactionViewClickListener? = null
    var userClickListener: MessageListView.UserClickListener? = null
    var readStateClickListener: ReadStateClickListener? = null
    var giphySendListener: GiphySendListener? = null

    var isThread = false
    var style: MessageListViewStyle? = null

    fun replaceEntities(newEntities: List<MessageListItem>) {
        val result = DiffUtil.calculateDiff(
            MessageListItemDiffCallback(messageListItemList, newEntities), true
        )
        result.dispatchUpdatesTo(this)
        messageListItemList = newEntities
    }

    override fun getItemCount(): Int = messageListItemList.size

    override fun getItemId(position: Int): Long = messageListItemList[position].getStableId()

    override fun getItemViewType(position: Int): Int {
        val messageListItem = messageListItemList[position]
        return viewHolderFactory.getMessageViewType(messageListItem)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseMessageListItemViewHolder<*> {
        return viewHolderFactory.createMessageViewHolder(this, parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseMessageListItemViewHolder<*>, position: Int) {
        holder.bindListItem(
            context,
            requireNotNull(channel) { "Channel was not set" },
            messageListItemList[position],
            requireNotNull(style) { "Style was not set" },
            requireNotNull(bubbleHelper) { "BubbleHelper was not set" },
            viewHolderFactory,
            position
        )
    }
}
