package io.getstream.chat.android.offline.experimental.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadState
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.adapter.ChatClientStateCalls
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState
import kotlinx.coroutines.CoroutineScope

/**
 * [StateRegistry] instance that contains all state objects exposed in offline plugin.
 */
internal val ChatClient.state: StateRegistry
    get() = requireNotNull(StateRegistry.get()) {
        "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
            "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
    }

/**
 * [LogicRegistry] instance that contains all objects responsible for handling logic in offline plugin.
 */
internal val ChatClient.logic: LogicRegistry
    get() = requireNotNull(LogicRegistry.get()) {
        "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
            "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
    }

/**
 * [GlobalState] instance that contains information about the current user, unreads, etc.
 */
public val ChatClient.globalState: GlobalState
    get() = GlobalMutableState.getOrCreate()

/**
 * Intermediate class to request ChatClient class as states
 *
 * @return [ChatClientStateCalls]
 */
internal fun ChatClient.requestsAsState(scope: CoroutineScope): ChatClientStateCalls =
    ChatClientStateCalls(this, state, scope)

/**
 * Same class of ChatClient.queryChannels, but provides the result as [QueryChannelsState]
 *
 * @param request [QueryChannelsRequest]
 * @return [QueryChannelsRequest]
 */
public fun ChatClient.queryChannelsAsState(
    request: QueryChannelsRequest,
    coroutineScope: CoroutineScope = state.scope,
): QueryChannelsState {
    return requestsAsState(coroutineScope).queryChannels(request)
}

/**
 * Same class of ChatClient.queryChannel, but provides the result as [ChannelState]
 *
 * @param cid
 * @return [ChannelState]
 */
public fun ChatClient.watchChannelAsState(
    cid: String,
    limit: Int,
    coroutineScope: CoroutineScope = state.scope,
): ChannelState {
    return requestsAsState(coroutineScope).watchChannel(cid, limit)
}

/**
 * Same class of ChatClient.getReplies, but provides the result as [ThreadState]
 *
 * @param cid
 * @return [ThreadState]
 */
public fun ChatClient.getRepliesAsState(
    cid: String,
    limit: Int,
    coroutineScope: CoroutineScope = state.scope,
): ThreadState {
    return requestsAsState(coroutineScope).getReplies(cid, limit)
}
