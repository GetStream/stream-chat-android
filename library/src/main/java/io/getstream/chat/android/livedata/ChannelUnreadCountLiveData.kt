package io.getstream.chat.android.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

// TODO: test me
class ChannelUnreadCountLiveData(val currentUser: User, val readLiveData: LiveData<ChannelUserRead>, val messagesLiveData: LiveData<List<Message>>): LiveData<Int>() {
    var read : ChannelUserRead? = null
    var messages : List<Message>? = null

    override fun observe(owner: LifecycleOwner, observer: Observer<in Int>) {
        super.observe(owner, observer)

        readLiveData.observe(owner, Observer { r ->
            read = r
            val count = calculateUnreadCount()
            postValue(count)
        })
        messagesLiveData.observe(owner, Observer { messages ->
            this.messages = messages
            val count = calculateUnreadCount()
            postValue(count)
        })

    }

    @Synchronized fun calculateUnreadCount(): Int {
        var unreadMessageCount = 0
         if (messages != null) {
            val lastRead = read?.lastRead
            val lastReadTime = lastRead?.time ?: 0
            val currentUserId = currentUser.id
            for (m in messages!!.reversed()) {
                if (m.user.id == currentUserId) continue
                if (m.deletedAt != null) continue
                if (m.extraData.getOrElse("silent") {false} == true) continue
                if (m.createdAt!!.time > lastReadTime) unreadMessageCount++
            }
        }
        return unreadMessageCount
    }
}