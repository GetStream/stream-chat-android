package com.getstream.sdk.chat.adapter

import androidx.recyclerview.widget.DiffUtil

class MessageListItemDiffCallback(
    private val oldList: List<MessageListItem>,
    private val newList: List<MessageListItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].getStableId() == newList[newItemPosition].getStableId()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
