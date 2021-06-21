@file:JvmName("ChannelUtils")

package com.getstream.sdk.chat.utils.extensions

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import java.util.Date

internal fun Channel.computeLastMessage(): Message? {
    val messages = messages
    for (i in messages.indices.reversed()) {
        val message = messages[i]
        if (message.deletedAt == null && message.type == ModelType.message_regular && !message.shadowed) {
            return message
        }
    }
    return null
}

internal fun Channel.getReadDateOfChannelLastMessage(userId: String?): Date? {
    val channelUserReadList = read
    if (!channelUserReadList.isNullOrEmpty()) {
        try {
            for (i in channelUserReadList.indices.reversed()) {
                val channelUserRead = channelUserReadList[i]
                if (channelUserRead.user.id == userId) {
                    return channelUserRead.lastRead
                }
            }
        } catch (e: Exception) {
            ChatLogger.instance.logE(e, "getReadDateOfChannelLastMessage")
        }
    }
    return null
}

@JvmOverloads
internal fun Channel.getChannelNameOrMembers(currentUser: User? = ChatDomain.instance().user.value): String {
    val userName = name
    return if (userName.isNotEmpty()) {
        userName
    } else {
        val users = members.getOtherUsers(currentUser)
        val userNames = users.take(3).map { it.name }
        userNames.joinToString(separator = ", ").let {
            if (users.size > 3) {
                "$it..."
            } else {
                it
            }
        }
    }
}

@JvmOverloads
internal fun Channel.readLastMessage(currentUser: User? = ChatDomain.instance().user.value): Boolean {
    val currentUserId = currentUser?.id
    val myReadDate: Date? = getReadDateOfChannelLastMessage(currentUserId)
    val lastMessage: Message? = computeLastMessage()
    return when {
        myReadDate == null -> false
        lastMessage == null -> true
        else -> {
            val lastMessageDate = lastMessage.createdAt ?: lastMessage.createdLocallyAt
            val lastMessageTime = lastMessageDate?.time ?: 0
            myReadDate.time >= lastMessageTime
        }
    }
}

@JvmOverloads
internal fun Channel.getLastMessageReads(
    currentUser: User? = ChatDomain.instance().user.value
): List<ChannelUserRead> {
    val lastMessage: Message? = computeLastMessage()
    if (lastMessage?.createdAt == null) return emptyList()

    val readLastMessage: MutableList<ChannelUserRead> = mutableListOf()

    val channelUserReadList = read
    val currentUserId = currentUser?.id
    for (channelUserRead in channelUserReadList) {
        if (channelUserRead.getUserId() == currentUserId || channelUserRead.lastRead == null) {
            continue
        }
        if (channelUserRead.lastRead!!.compareTo(lastMessage.createdAt) > -1) {
            readLastMessage.add(channelUserRead)
        }
    }

    readLastMessage.sortWith { o1, o2 -> o1.lastRead!!.compareTo(o2.lastRead) }
    return readLastMessage
}
