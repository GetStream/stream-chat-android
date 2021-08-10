package io.getstream.chat.android.offline.plugin

import io.getstream.chat.android.client.call.Call
import kotlinx.coroutines.CoroutineScope

public interface QueryReference<T : Any, S : Any> {
    public fun get(): Call<T>
    public fun asState(scope: CoroutineScope): S
}
