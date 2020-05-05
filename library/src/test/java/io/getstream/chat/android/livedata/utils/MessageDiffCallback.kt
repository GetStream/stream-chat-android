package io.getstream.chat.android.livedata.utils

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.client.models.Message

data class MessageDiffCallback(
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
        val sameId = old[oldItemPosition].id == new[newItemPosition].id
        //println("${old[oldItemPosition].id} areItemsTheSame ${new[newItemPosition].id} ${sameId}")
        return sameId
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val same = old[oldItemPosition] == new[newItemPosition]
        //println("${old[oldItemPosition].id} areContentsTheSame ${new[newItemPosition].id} $same")

        return same
    }

}
