package com.getstream.sdk.chat.adapter

import androidx.recyclerview.widget.DiffUtil

class MessageListItemDiffCallback(
    private val old: List<MessageListItem>,
    private val new: List<MessageListItem>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return old[oldItemPosition].getStableId() == new[newItemPosition].getStableId()
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}
