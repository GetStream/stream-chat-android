package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.channel.ChannelData
import kotlinx.coroutines.flow.map
import io.getstream.chat.android.offline.channel.ChannelController as ChannelControllerStateFlow
import io.getstream.chat.android.offline.channel.ChannelController.MessagesState as OfflineMessageState

internal class ChannelControllerImpl(private val channelControllerStateFlow: ChannelControllerStateFlow) :
    ChannelController {

    override val channelType: String = channelControllerStateFlow.channelType
    override val channelId: String = channelControllerStateFlow.channelId

    override val repliedMessage: LiveData<Message?> = channelControllerStateFlow.repliedMessage.asLiveData()
    override val messages: LiveData<List<Message>> = channelControllerStateFlow.messages.asLiveData()
    override val messagesState = channelControllerStateFlow.messagesState.map {
        when (it) {
            OfflineMessageState.Loading -> ChannelController.MessagesState.Loading
            OfflineMessageState.NoQueryActive -> ChannelController.MessagesState.NoQueryActive
            OfflineMessageState.OfflineNoResults -> ChannelController.MessagesState.OfflineNoResults
            is OfflineMessageState.Result -> ChannelController.MessagesState.Result(it.messages)
        }
    }.asLiveData()
    override val oldMessages: LiveData<List<Message>> = channelControllerStateFlow.oldMessages.asLiveData()
    override val watcherCount: LiveData<Int> = channelControllerStateFlow.watcherCount.asLiveData()
    override val watchers: LiveData<List<User>> = channelControllerStateFlow.watchers.asLiveData()
    override val typing: LiveData<TypingEvent> = channelControllerStateFlow.typing.asLiveData()
    override val reads: LiveData<List<ChannelUserRead>> = channelControllerStateFlow.reads.asLiveData()
    override val read: LiveData<ChannelUserRead?> = channelControllerStateFlow.read.asLiveData()
    override val unreadCount: LiveData<Int?> = channelControllerStateFlow.unreadCount.asLiveData()
    override val members: LiveData<List<Member>> = channelControllerStateFlow.members.asLiveData()
    override val offlineChannelData: LiveData<ChannelData> = channelControllerStateFlow.channelData.asLiveData()
    override val hidden: LiveData<Boolean> = channelControllerStateFlow.hidden.asLiveData()
    override val muted: LiveData<Boolean> = channelControllerStateFlow.muted.asLiveData()
    override val loading: LiveData<Boolean> = channelControllerStateFlow.loading.asLiveData()
    override val loadingOlderMessages: LiveData<Boolean> = channelControllerStateFlow.loadingOlderMessages.asLiveData()
    override val loadingNewerMessages: LiveData<Boolean> = channelControllerStateFlow.loadingNewerMessages.asLiveData()
    override val endOfOlderMessages: LiveData<Boolean> = channelControllerStateFlow.endOfOlderMessages.asLiveData()
    override val endOfNewerMessages: LiveData<Boolean> = channelControllerStateFlow.endOfNewerMessages.asLiveData()

    override var recoveryNeeded = channelControllerStateFlow.recoveryNeeded
    override val cid = channelControllerStateFlow.cid

    override fun getMessage(messageId: String): Message? = channelControllerStateFlow.getMessage(messageId)
    override fun clean() = channelControllerStateFlow.clean()
    override fun toChannel(): Channel = channelControllerStateFlow.toChannel()
}
