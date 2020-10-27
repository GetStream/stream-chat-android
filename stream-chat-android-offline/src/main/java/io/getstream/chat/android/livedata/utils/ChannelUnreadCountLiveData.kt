package io.getstream.chat.android.livedata.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.MessagesUpdate
import io.getstream.chat.android.client.models.User

internal class ChannelUnreadCountLiveData(
    val currentUser: User,
    private val readLiveData: LiveData<ChannelUserRead>,
    private val messagesLiveData: LiveData<MessagesUpdate>
) : LiveData<Int>() {
    var read: ChannelUserRead? = null
    var messages: List<Message>? = null

    private val readObserver = Observer<ChannelUserRead> { read ->
        this.read = read
        calculateUnreadCount()?.let { value = it }
    }

    private val messageObserver = Observer<MessagesUpdate> { messagesUpdate ->
        this.messages = messagesUpdate.messages
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
