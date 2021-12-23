package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.model.ConnectionState

public abstract class BaseMessageListHeaderViewModel @InternalStreamChatApi constructor(
    cid: String,
    private val chatDomain: ChatDomain,
) : ViewModel() {

    private val _activeThread = MutableLiveData<Message?>()
    private val _members = MediatorLiveData<List<Member>>()
    private val _channelState = MediatorLiveData<Channel>()
    private val _anyOtherUsersOnline = MediatorLiveData<Boolean>()
    private val _typingUsers = MediatorLiveData<List<User>>()

    public val activeThread: LiveData<Message?> = _activeThread
    public val members: LiveData<List<Member>> = _members
    public val channelState: LiveData<Channel> = _channelState
    public val anyOtherUsersOnline: LiveData<Boolean> = _anyOtherUsersOnline
    public val online: LiveData<ConnectionState> = chatDomain.connectionState
    public val typingUsers: LiveData<List<User>> = _typingUsers

    private val logger = ChatLogger.get("MessageListHeaderViewModel")

    init {
        chatDomain.watchChannel(cid, 0).enqueue { channelControllerResult ->
            if (channelControllerResult.isSuccess) {
                val channelController = channelControllerResult.data()
                _members.addSource(channelController.members) { _members.value = it }
                _channelState.addSource(map(channelController.offlineChannelData) { channelController.toChannel() }) {
                    _channelState.value = it
                }
                _channelState.addSource(map(channelController.members) { channelController.toChannel() }) {
                    _channelState.value = it
                }
                _anyOtherUsersOnline.addSource(
                    map(channelController.members) { members ->
                        members.asSequence()
                            .filter { it.user != chatDomain.user.value }
                            .any { it.user.online }
                    }
                ) { _anyOtherUsersOnline.value = it }
                _typingUsers.addSource(channelController.typing) { typingEvent ->
                    _typingUsers.value = typingEvent.users
                }
            } else {
                logger.logE("Could not watch channel with cid: $cid. Error: ${channelControllerResult.error()}")
            }
        }
    }

    public fun setActiveThread(message: Message) {
        _activeThread.postValue(message)
    }

    public fun resetThread() {
        _activeThread.postValue(null)
    }
}
