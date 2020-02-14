package io.getstream.chat.android.client.api

import android.content.Context
import io.getstream.chat.android.client.ChatModules
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.token.CachedTokenProvider
import io.getstream.chat.android.client.token.CachedTokenProviderImpl
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.ImmediateTokenProvider


class ChatConfig(
    val apiKey: String
) {

    private var baseUrlEndpoint: String = ""
    private var cdnUrlEndpoint: String = ""
    var baseTimeout: Long = 0
    var cdnTimeout: Long = 0
    internal lateinit var modules: ChatModules

    val tokenProvider: CachedTokenProvider = CachedTokenProviderImpl()
    lateinit var notificationsConfig: ChatNotificationConfig

    var isAnonymous: Boolean = false

    val httpURL: String
        get() = "https://$baseUrlEndpoint/"

    val cdnHttpURL: String
        get() = "https://$cdnUrlEndpoint/"

    val wssURL: String
        get() = "wss://$baseUrlEndpoint/"

    class Builder {

        private val apiKey: String
        private val appContext: Context

        private var baseUrl: String = "chat-us-east-1.stream-io-api.com"
        private var cdnUrl: String = baseUrl
        private var baseTimeout = 10000L
        private var cdnTimeout = 10000L
        private var tokenProviderInstance: TokenProvider
        private lateinit var notificationsConfig: ChatNotificationConfig

        constructor(apiKey: String, token: String, appContext: Context) {
            this.apiKey = apiKey
            this.tokenProviderInstance = ImmediateTokenProvider(token)
            this.appContext = appContext
        }

        constructor(apiKey: String, tokenProvider: TokenProvider, appContext: Context) {
            this.apiKey = apiKey
            this.tokenProviderInstance = tokenProvider
            this.appContext = appContext
        }

        fun notifications(notificationsConfig: ChatNotificationConfig): Builder {
            this.notificationsConfig = notificationsConfig
            return this
        }

        fun baseTimeout(timeout: Long): Builder {
            baseTimeout = timeout
            return this
        }

        fun cdnTimeout(timeout: Long): Builder {
            cdnTimeout = timeout
            return this
        }

        fun baseUrl(value: String): Builder {
            var baseUrl = value
            if (baseUrl.startsWith("https://")) {
                baseUrl = baseUrl.split("https://").toTypedArray()[1]
            }
            if (baseUrl.startsWith("http://")) {
                baseUrl = baseUrl.split("http://").toTypedArray()[1]
            }
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length - 1)
            }
            this.baseUrl = baseUrl
            return this
        }

        fun cdnUrl(value: String): Builder {
            var cdnUrl = value
            if (cdnUrl.startsWith("https://")) {
                cdnUrl = cdnUrl.split("https://").toTypedArray()[1]
            }
            if (cdnUrl.startsWith("http://")) {
                cdnUrl = cdnUrl.split("http://").toTypedArray()[1]
            }
            if (cdnUrl.endsWith("/")) {
                cdnUrl = cdnUrl.substring(0, cdnUrl.length - 1)
            }
            this.cdnUrl = cdnUrl
            return this
        }

        fun build(): ChatConfig {

            if (apiKey.isEmpty()) {
                throw ChatError("apiKey is not defined in ChatConfig.Builder")
            }

            val result = ChatConfig(apiKey)

            if (!this::notificationsConfig.isInitialized) {
                result.notificationsConfig = ChatNotificationConfig(appContext)
            } else {
                result.notificationsConfig = notificationsConfig
            }

            result.modules = ChatModules(result)
            result.baseUrlEndpoint = baseUrl
            result.cdnUrlEndpoint = cdnUrl
            result.baseTimeout = baseTimeout
            result.cdnTimeout = cdnTimeout

            result.tokenProvider.setTokenProvider(tokenProviderInstance)
            return result
        }
    }
}