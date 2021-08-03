package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.QueryChannelsReference
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.offline.querychannels.state.QueryChannelsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public fun QueryChannelsReference.asState(scope: CoroutineScope): QueryChannelsState {
    scope.launch {
        get().await()
    }

    return ChatClient.instance().state.queryChannels(request.filter, request.querySort)
}
