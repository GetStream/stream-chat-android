package io.getstream.chat.android.offline.experimental

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadState
import io.getstream.chat.android.offline.experimental.extensions.state
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState

internal fun <T : Any, R> Call<T>.callAsState(stateProvider: () -> R): R {
    this.enqueue()
    return stateProvider.invoke()
}

public fun Call<List<Channel>>.threadState(messageId: String): ThreadState {
    val state = StateRegistry.get()
    return callAsState { state.thread(messageId) }
}

public fun Call<List<Channel>>.queryChannelsState(request: QueryChannelsRequest): QueryChannelsState {
    val state = StateRegistry.get()
    return callAsState { state.queryChannels(request.filter, request.querySort) }
}
