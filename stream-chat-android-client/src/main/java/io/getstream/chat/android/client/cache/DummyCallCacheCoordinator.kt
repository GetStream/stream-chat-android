package io.getstream.chat.android.client.cache

import io.getstream.chat.android.client.call.Call

internal class DummyCallCacheCoordinator: CacheCoordinator {

    override fun <T : Any> cachedCall(hashCode: Int, forceRefresh: Boolean, call: Call<T>): Call<T> = call
}
