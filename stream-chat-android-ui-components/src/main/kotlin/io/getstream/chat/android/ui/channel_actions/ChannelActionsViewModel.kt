package io.getstream.chat.android.ui.channel_actions

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController

internal class ChannelActionsViewModel(
    cid: String,
    isGroup: Boolean,
    chatDomain: ChatDomain = ChatDomain.instance()
) : ViewModel() {
    val members: LiveData<List<Member>>

    init {
        val channelController: ChannelController = chatDomain.useCases
            .watchChannel(cid, 0)
            .execute()
            .data()
        members = if (isGroup) {
            channelController.members
        } else {
            Transformations.map(channelController.members) { members ->
                members.filter { it.user.id != chatDomain.currentUser.id }
            }
        }
    }
}
