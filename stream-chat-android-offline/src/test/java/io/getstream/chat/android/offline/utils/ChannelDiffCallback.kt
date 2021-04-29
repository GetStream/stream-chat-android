package io.getstream.chat.android.offline.utils

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.client.models.Channel

internal data class ChannelDiffCallback(
    var oldChannels: List<Channel>,
    var newChannels: List<Channel>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldChannels.size
    }

    override fun getNewListSize(): Int {
        return newChannels.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldChannels[oldItemPosition].id == newChannels[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return oldChannels[oldItemPosition] == newChannels[newItemPosition]
    }
}
