package io.getstream.chat.android.offline.experimental.plugin.query

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope

@InternalStreamChatApi
/**
 * Generic reference entity that wrap a request to ChatClient and expose offline state (see [asState]).
 * [T] is a type of requested data.
 * [S] is a offline state.
 */
public interface QueryReference<T : Any, S : Any> {
    /** Returns a call as result of request */
    public fun get(): Call<T>

    /**
     * Returns an offline state representing this request.
     *
     * @param scope Coroutine scope where initial data filling action is being invoked.
     */
    public fun asState(scope: CoroutineScope): S
}
