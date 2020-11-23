package com.getstream.sdk.chat.adapter

import androidx.recyclerview.widget.DiffUtil
import com.getstream.sdk.chat.utils.extensions.computeLastMessage
import com.getstream.sdk.chat.utils.extensions.getOtherUsers
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain.Companion.instance

internal class ChannelListDiffCallback @JvmOverloads constructor(
    private val oldList: List<Channel>,
    private val newList: List<Channel>,
    private val currentUser: User = instance().currentUser
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].cid == newList[newItemPosition].cid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldChannel = oldList[oldItemPosition]
        val newChannel = newList[newItemPosition]

        var contentTheSame = true

        if (oldChannel.cid != newChannel.cid) {
            contentTheSame = false
        } else if (oldChannel.updatedAt == null && newChannel.updatedAt != null) {
            contentTheSame = false
        } else if (newChannel.updatedAt != null && oldChannel.updatedAt!!.time < newChannel.updatedAt!!.time) {
            contentTheSame = false
        } else if (oldChannel.extraData != newChannel.extraData) {
            contentTheSame = false
        } else if (!lastMessagesAreTheSame(oldChannel, newChannel)) {
            contentTheSame = false
        } else if (channelUserReadIsDifferent(oldChannel, newChannel)) {
            contentTheSame = false
        }
        return contentTheSame
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldChannel = oldList[oldItemPosition]
        val newChannel = newList[newItemPosition]
        return ChannelItemPayloadDiff(
            lastMessage = !lastMessagesAreTheSame(oldChannel, newChannel),
            name = !channelNameIsTheSame(newChannel, oldChannel),
            avatarView = !channelUsersAreTheSame(newChannel, oldChannel),
            readState = channelUserReadIsDifferent(oldChannel, newChannel),
            lastMessageDate = !channelLastMessageDatesAreTheSame(oldChannel, newChannel)
        )
    }

    private fun lastMessagesAreTheSame(oldChannel: Channel, newChannel: Channel): Boolean {
        return oldChannel.computeLastMessage() == newChannel.computeLastMessage()
    }

    private fun channelNameIsTheSame(oldChannel: Channel, newChannel: Channel): Boolean {
        return oldChannel.name == newChannel.name
    }

    private fun channelUsersAreTheSame(oldChannel: Channel, newChannel: Channel): Boolean {
        val oldUserList = oldChannel.members.getOtherUsers(currentUser)
        val newUserList = newChannel.members.getOtherUsers(currentUser)

        if (oldUserList.size != newUserList.size) return false
        if (oldUserList.isEmpty() && newUserList.isEmpty()) return true

        for (i in oldUserList.indices) {
            if (oldUserList[i].id != newUserList[i].id) {
                return false
            }
        }
        return true
    }

    private fun channelLastMessageDatesAreTheSame(oldChannel: Channel, newChannel: Channel): Boolean {
        return oldChannel.lastMessageAt == newChannel.lastMessageAt
    }

    private fun channelUserReadIsDifferent(oldChannel: Channel, newChannel: Channel): Boolean {
        val oldRead = getUserRead(oldChannel, currentUser.id)
        val newRead = getUserRead(newChannel, currentUser.id)
        return if (oldRead == null || newRead == null) {
            false
        } else {
            newRead.lastRead!!.after(oldRead.lastRead)
        }
    }

    private fun getUserRead(channel: Channel, userId: String): ChannelUserRead? {
        return channel.read.firstOrNull { it.getUserId() == userId }
    }
}
