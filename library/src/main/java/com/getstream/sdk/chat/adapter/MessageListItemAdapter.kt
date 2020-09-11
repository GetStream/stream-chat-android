package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

class MessageListItemAdapter @JvmOverloads constructor(
    var viewHolderFactory: MessageViewHolderFactory,
    var channel: Channel? = null,
    private var messageListItemList: List<MessageListItem> = emptyList()
) : RecyclerView.Adapter<BaseMessageListItemViewHolder<*>>() {

    var bubbleHelper: BubbleHelper? = null
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
        return viewHolderFactory.createMessageViewHolder(
            parent = parent,
            viewType = viewType,
            style = requireNotNull(style) { "Style was not set" }
        )
    }

    override fun onBindViewHolder(holder: BaseMessageListItemViewHolder<*>, position: Int) {
        holder.bindListItem(
            requireNotNull(channel) { "Channel was not set" },
            messageListItemList[position],
            requireNotNull(bubbleHelper) { "BubbleHelper was not set" },
            viewHolderFactory,
            position
        )
    }
}
