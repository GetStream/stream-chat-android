package io.getstream.chat.android.offline.plugin

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope

@InternalStreamChatApi
@ExperimentalStreamChatApi
public interface QueryReference<T : Any, S : Any> {
    public fun get(): Call<T>
    public fun asState(scope: CoroutineScope): S
}
