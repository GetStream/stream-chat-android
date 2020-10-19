package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController

private const val MESSAGE_LIMIT = 30

public class ChannelHeaderViewModel @JvmOverloads constructor(
    cid: String,
    messageLimit: Int = MESSAGE_LIMIT,
    private val chatDomain: ChatDomain = ChatDomain.instance()
) : ViewModel() {

    public val members: LiveData<List<Member>>
    public val channelState: LiveData<Channel>
    public val anyOtherUsersOnline: LiveData<Boolean>

    init {
        val channelController: ChannelController =
            chatDomain.useCases.watchChannel.invoke(cid, 0).execute().data()
        members = channelController.members
        channelState = map(channelController.channelData) { channelController.toChannel() }
        anyOtherUsersOnline = map(members) { members ->
            members.asSequence()
                .filter { it.user != chatDomain.currentUser }
                .any { it.user.online }
        }
    }
}
