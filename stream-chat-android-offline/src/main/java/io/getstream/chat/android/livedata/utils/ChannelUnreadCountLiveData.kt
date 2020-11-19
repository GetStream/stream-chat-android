package io.getstream.chat.android.livedata.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

// TODO: replace with flow
internal class ChannelUnreadCountLiveData(
    val currentUser: User,
    val readLiveData: LiveData<ChannelUserRead>,
    val messagesLiveData: LiveData<List<Message>>
) : LiveData<Int>() {
    var read: ChannelUserRead? = null
    var messages: List<Message>? = null

    val readObserver = Observer<ChannelUserRead> { read ->
        this.read = read
        calculateUnreadCount()?.let { value = it }
    }

    val messageObserver = Observer<List<Message>> { messages ->
        this.messages = messages
        calculateUnreadCount()?.let { value = it }
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
        return computeUnreadCount(currentUser, read, messages)
    }
}
