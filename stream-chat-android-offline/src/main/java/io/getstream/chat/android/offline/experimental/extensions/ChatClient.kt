package io.getstream.chat.android.offline.experimental.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadState
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.adapter.ChatClientStateCalls
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState

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

@InternalStreamChatApi
internal fun ChatClient.requestsAsState(): ChatClientStateCalls = ChatClientStateCalls(this, state)

public fun ChatClient.queryChannelsAsState(request: QueryChannelsRequest): QueryChannelsState {
    return requestsAsState().queryChannels(request)
}

public fun ChatClient.watchChannelAsState(cid: String, limit: Int = DEFAULT_MESSAGE_LIMIT): ChannelState {
    return requestsAsState().watchChannel(cid, limit)
}

public fun ChatClient.getRepliesAsState(cid: String, limit: Int = DEFAULT_MESSAGE_LIMIT): ThreadState {
    return requestsAsState().getReplies(cid, limit)
}

public const val DEFAULT_MESSAGE_LIMIT: Int = 30
