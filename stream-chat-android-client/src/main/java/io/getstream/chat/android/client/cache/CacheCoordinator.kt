package io.getstream.chat.android.client.cache

import io.getstream.chat.android.client.call.Call

public interface CacheCoordinator {

    public fun <T : Any> cachedCall(hashCode: Int, forceRefresh: Boolean, call: Call<T>): Call<T>
}
