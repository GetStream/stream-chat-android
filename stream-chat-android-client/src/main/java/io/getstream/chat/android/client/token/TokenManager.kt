package io.getstream.chat.android.client.token

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.utils.Result

internal interface TokenManager {

    @UiThread
    fun loadAsync(listener: (Result<String>) -> Unit = {})

    @UiThread
    fun loadAsync()

    @WorkerThread
    fun loadSync()

    fun expireToken()
    fun setTokenProvider(provider: TokenProvider)
    fun getToken(): String
    fun hasToken(): Boolean
    fun hasTokenProvider(): Boolean
}
