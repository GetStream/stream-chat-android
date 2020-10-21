package io.getstream.chat.android.client.sample.common

import androidx.recyclerview.widget.DiffUtil

internal class DiffCallback(
    private val oldData: List<Channel>,
    private val newData: List<Channel>
) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldData.size
    }

    override fun getNewListSize(): Int {
        return newData.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldData[oldItemPosition].remoteId == newData[newItemPosition].remoteId
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldData[oldItemPosition].updatedAt == newData[newItemPosition].updatedAt
    }
}
