package io.getstream.chat.android.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

class ChannelUnreadCountLiveData(
    val currentUser: User,
    val readLiveData: LiveData<ChannelUserRead>,
    val messagesLiveData: LiveData<List<Message>>
) : LiveData<Int>() {
    var read: ChannelUserRead? = null
    var messages: List<Message>? = null

    val readObserver = Observer<ChannelUserRead> { r ->
        read = r
        val count = calculateUnreadCount()
        if (count != null) {
            value = count
        }
    }

    val messageObserver = Observer<List<Message>> { messages ->
        this.messages = messages
        val count = calculateUnreadCount()
        if (count != null) {
            value = count
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in Int>) {
        super.observe(owner, observer)

        readLiveData.observe(owner, readObserver)
        messagesLiveData.observe(owner, messageObserver)
    }

    override fun observeForever(observer: Observer<in Int>) {
        super.observeForever(observer)

        readLiveData.observeForever(readObserver)
        messagesLiveData.observeForever(messageObserver)
    }

    @Synchronized
    fun calculateUnreadCount(): Int? {
        var unreadMessageCount: Int? = null
        if (messages != null && read != null) {
            unreadMessageCount = 0
            val lastRead = read?.lastRead
            val lastReadTime = lastRead?.time ?: 0
            val currentUserId = currentUser.id
            for (m in messages!!.reversed()) {
                if (m.user.id == currentUserId) continue
                if (m.deletedAt != null) continue
                if (m.extraData.getOrElse("silent") { false } == true) continue
                if (m.createdAt!!.time > lastReadTime) unreadMessageCount++
            }
        }
        return unreadMessageCount
    }
}
