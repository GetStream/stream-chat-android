package io.getstream.chat.android.offline.utils

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.client.models.Message

internal data class MessageDiffCallback(
    var old: List<Message>,
    var new: List<Message>
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
        return old[oldItemPosition].id == new[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}
