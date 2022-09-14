package io.getstream.chat.android.offline.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
public interface QueryChannelsStateLogic {

    public fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult

    public fun isLoading(): Boolean

    public fun getChannelsOffset(): Int

    public fun getChannels(): Map<String, Channel>?

    public fun getQuerySpecs(): QueryChannelsSpec

    public fun getState(): QueryChannelsState

    public fun setLoading(isLoading: Boolean)

    public fun setCurrentRequest(request: QueryChannelsRequest)

    public fun setEndOfChannels(isEnd: Boolean)

    public fun setRecoveryNeeded(recoveryNeeded: Boolean)

    public fun setChannelsOffset(offset: Int)

    public fun incrementChannelsOffset(size: Int)

    public fun loadingForCurrentRequest(): MutableStateFlow<Boolean>

    public fun addChannelsState(channels: List<Channel>)

    public fun removeChannels(cidSet: Set<String>)

    public fun initializeChannelsIfNeeded()

    public fun refreshChannels(cidList: Collection<String>)

    public fun refreshMembersStateForUser(newUser: User)
}
