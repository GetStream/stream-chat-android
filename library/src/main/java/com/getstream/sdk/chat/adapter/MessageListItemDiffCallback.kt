package com.getstream.sdk.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.livedata.utils.MessageListItem

object MessageListItemDiffCallback : DiffUtil.ItemCallback<MessageListItem>() {
    override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return oldItem.getStableId() == newItem.getStableId()
    }

    override fun areContentsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
        return oldItem == newItem
    }
}
