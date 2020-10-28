package io.getstream.chat.android.client.token

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import java.util.concurrent.Executors
import java.util.concurrent.Future

internal class TokenManagerImpl : TokenManager {

    @Volatile private var token: String = ""
    @Volatile private var loadingFuture: Future<*>? = null
    private val listeners = mutableListOf<((Result<String>) -> Unit)?>()
    private lateinit var provider: TokenProvider
    private val tokenProviderExecutor = Executors.newSingleThreadExecutor()

    override fun loadAsync(listener: (Result<String>) -> Unit) {

        if (hasToken()) {
            listener(Result(token))
        } else {
            listeners.add(listener)
            loadAsync()
        }
    }

    override fun loadAsync() {
        if (loadingFuture == null) {
            loadingFuture = tokenProviderExecutor.submit {
                loadSync()
                loadingFuture = null
            }
        }
    }

    override fun loadSync() {
        try {
            onTokenLoaded(provider.loadToken())
        } catch (t: Throwable) {
            onTokenLoadingError(t)
        }
    }

    override fun getToken(): String {
        return token
    }

    override fun expireToken() {
        token = ""
    }

    override fun setTokenProvider(provider: TokenProvider) {
        this.token = ""
        this.provider = provider
    }

    override fun hasToken(): Boolean {
        return token.isNotEmpty()
    }

    override fun hasTokenProvider(): Boolean {
        return this::provider.isInitialized
    }

    /**
     * This method is synchronized because this class is not thread safe. Without
     * synchronization concurrent invocations of [loadAsync] and [loadSync] can lead
     * to the state when [listeners] are notified several times.
     */
    @Synchronized
    private fun onTokenLoaded(token: String?) {
        this.token = token ?: ""
        listeners.forEach { it?.invoke(Result(this.token)) }
        listeners.clear()
    }

    private fun onTokenLoadingError(cause: Throwable) {
        token = ""
        listeners.forEach { it?.invoke(Result(ChatError("Unable to load token", cause))) }
        listeners.clear()
    }
}
