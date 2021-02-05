package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

/**
 * ViewModel class for [com.getstream.sdk.chat.view.ChannelHeaderView].
 * Responsible for updating channel information.
 * Can be bound to the view using [ChannelHeaderViewModel.bindView] function.
 * @param cid the full channel id, i.e. "messaging:123"
 * @param chatDomain entry point for all livedata & offline operations
 */
public class ChannelHeaderViewModel @JvmOverloads constructor(
    cid: String,
    private val chatDomain: ChatDomain = ChatDomain.instance()
) : ViewModel() {

    private var _activeThread = MutableLiveData<Message?>()
    private val _members = MediatorLiveData<List<Member>>()
    private val _channelState = MediatorLiveData<Channel>()
    private val _anyOtherUsersOnline = MediatorLiveData<Boolean>()
    private val _typingUsers = MediatorLiveData<List<User>>()

    public var activeThread: LiveData<Message?> = _activeThread
    public val members: LiveData<List<Member>> = _members
    public val channelState: LiveData<Channel> = _channelState
    public val anyOtherUsersOnline: LiveData<Boolean> = _anyOtherUsersOnline
    public val online: LiveData<Boolean> = chatDomain.online
    public val typingUsers: LiveData<List<User>> = _typingUsers

    init {
        chatDomain.useCases.watchChannel(cid, 0).enqueue { channelControllerResult ->
            if (channelControllerResult.isSuccess) {
                val channelController = channelControllerResult.data()
                _members.addSource(channelController.members) { _members.value = it }
                _channelState.addSource(map(channelController.channelData) { channelController.toChannel() }) {
                    _channelState.value = it
                }
                _anyOtherUsersOnline.addSource(
                    map(channelController.members) { members ->
                        members.asSequence()
                            .filter { it.user != chatDomain.currentUser }
                            .any { it.user.online }
                    }
                ) { _anyOtherUsersOnline.value = it }
                _typingUsers.addSource(channelController.typing) { typingEvent ->
                    _typingUsers.value = typingEvent.users
                }
            }
        }
    }

    public fun setActiveThread(message: Message?) {
        _activeThread.postValue(message)
    }
}
