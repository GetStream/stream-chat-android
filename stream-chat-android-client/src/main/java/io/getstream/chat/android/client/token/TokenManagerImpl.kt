package io.getstream.chat.android.client.token

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class TokenManagerImpl : TokenManager {
    @Volatile
    private var token: String = EMPTY_TOKEN
    private lateinit var provider: TokenProvider
    private var job: Job? = null

    override fun loadAsyncAndRetry(callback: (Result<String>) -> Unit) {
        job?.cancel()
        job = GlobalScope.launch(DispatcherProvider.IO) {
            val result = if (hasToken()) {
                Result(token)
            } else {
                var newToken: String = runAndRetry()
                if (newToken != EMPTY_TOKEN) {
                    Result(newToken)
                } else {
                    Result(ChatError("Unable to load token"))
                }
            }
            withContext(DispatcherProvider.Main) {
                callback(result)
            }
        }
    }

    @Synchronized
    override fun loadSync(): String {
        return try {
            provider.loadToken()
        } catch (t: Throwable) {
            EMPTY_TOKEN
        }.also {
            this.token = it
        }
    }

    override fun getToken(): String = token

    override fun expireToken() {
        token = EMPTY_TOKEN
    }

    override fun setTokenProvider(provider: TokenProvider) {
        this.token = EMPTY_TOKEN
        this.provider = provider
    }

    override fun hasToken(): Boolean = token != EMPTY_TOKEN

    override fun hasTokenProvider(): Boolean {
        return this::provider.isInitialized
    }

    override fun shutdown() {
        job?.cancel()
    }

    private suspend fun runAndRetry(): String {
        var attempt = 1
        var token: String = EMPTY_TOKEN

        while (attempt < ATTEMPT_COUNT) {
            token = loadSync()
            if (token != EMPTY_TOKEN) {
                break
            } else {
                delay(attempt * RETRY_DELAY_MILLIS)
                attempt += 1
            }
        }
        return token
    }

    companion object {
        private const val EMPTY_TOKEN = ""
        private const val RETRY_DELAY_MILLIS = 2000L
        private const val ATTEMPT_COUNT = 4
    }
}
