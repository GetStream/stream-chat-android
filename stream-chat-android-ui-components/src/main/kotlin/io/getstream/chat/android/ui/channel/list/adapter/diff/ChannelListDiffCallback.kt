package io.getstream.chat.android.ui.channel.list.adapter.diff

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.utils.extensions.getLastMessage
import io.getstream.chat.android.ui.utils.extensions.getLastMessageReadCount
import io.getstream.chat.android.ui.utils.extensions.getUsers
public class ChannelListDiffCallback(
    private val oldList: List<Channel>,
    private val newList: List<Channel>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].cid == newList[newItemPosition].cid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldChannel = oldList[oldItemPosition]
        val newChannel = newList[newItemPosition]

        return listOf(
            cidEqual(oldChannel, newChannel),
            nameEqual(oldChannel, newChannel),
            userEqual(oldChannel, newChannel),
            updateEqual(oldChannel, newChannel),
            extraDataEqual(oldChannel, newChannel),
            lastMessageEqual(oldChannel, newChannel),
            lastReadEqual(oldChannel, newChannel),
        ).all { it }
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldChannel = oldList[oldItemPosition]
        val newChannel = newList[newItemPosition]

        return ChannelItemDiff(
            nameChanged = !nameEqual(oldChannel, newChannel),
            avatarViewChanged = !userEqual(oldChannel, newChannel),
            readStateChanged = !lastReadEqual(oldChannel, newChannel),
            lastMessageChanged = !lastMessageEqual(oldChannel, newChannel)
        )
    }

    private fun cidEqual(oldChannel: Channel, newChannel: Channel) =
        oldChannel.cid == newChannel.cid

    private fun nameEqual(oldChannel: Channel, newChannel: Channel) =
        oldChannel.name == newChannel.name

    private fun userEqual(oldChannel: Channel, newChannel: Channel) =
        oldChannel.getUsers() == newChannel.getUsers()

    private fun updateEqual(oldChannel: Channel, newChannel: Channel) =
        oldChannel.updatedAt?.equals(newChannel.updatedAt) == true

    private fun extraDataEqual(oldChannel: Channel, newChannel: Channel) =
        oldChannel.extraData == newChannel.extraData

    private fun lastMessageEqual(oldChannel: Channel, newChannel: Channel) =
        oldChannel.getLastMessage() == newChannel.getLastMessage()

    private fun lastReadEqual(oldChannel: Channel, newChannel: Channel) =
        oldChannel.getLastMessageReadCount() == newChannel.getLastMessageReadCount()
}
