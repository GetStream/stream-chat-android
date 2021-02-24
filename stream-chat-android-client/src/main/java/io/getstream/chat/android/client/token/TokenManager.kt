package io.getstream.chat.android.client.token

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.utils.Result

internal interface TokenManager {

    @UiThread
    fun loadAsyncAndRetry(callback: (Result<String>) -> Unit = {})

    @WorkerThread
    fun loadSync(): String

    fun expireToken()
    fun setTokenProvider(provider: TokenProvider)
    fun getToken(): String
    fun hasToken(): Boolean
    fun hasTokenProvider(): Boolean
    fun shutdown()
}
