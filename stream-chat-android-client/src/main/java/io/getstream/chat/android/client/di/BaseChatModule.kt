package io.getstream.chat.android.client.di

import android.content.Context
import android.net.ConnectivityManager
import com.moczul.ok2curl.CurlInterceptor
import io.getstream.chat.android.client.api.AnonymousApi
import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.GsonChatApi
import io.getstream.chat.android.client.api.HeadersInterceptor
import io.getstream.chat.android.client.api.HttpLoggingInterceptor
import io.getstream.chat.android.client.api.RetrofitAnonymousApi
import io.getstream.chat.android.client.api.RetrofitApi
import io.getstream.chat.android.client.api.RetrofitCallAdapterFactory
import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.TokenAuthInterceptor
import io.getstream.chat.android.client.api2.MessageApi
import io.getstream.chat.android.client.api2.MoshiApi
import io.getstream.chat.android.client.api2.MoshiChatApi
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser.GsonChatParser
import io.getstream.chat.android.client.parser2.MoshiChatParser
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketImpl
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.StreamFileUploader
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal open class BaseChatModule(
    private val appContext: Context,
    private val config: ChatClientConfig,
    private val notificationsHandler: ChatNotificationHandler,
    private val fileUploader: FileUploader? = null,
    private val tokenManager: TokenManager = TokenManagerImpl(),
) {

    private val defaultLogger: ChatLogger = ChatLogger.Builder(config.loggerConfig).build()

    private val gsonParser: ChatParser by lazy { GsonChatParser() }
    private val moshiParser: ChatParser by lazy { MoshiChatParser() }

    private val defaultNotifications by lazy {
        buildNotification(notificationsHandler, api())
    }
    private val defaultApi by lazy { buildApi(config) }
    private val defaultSocket by lazy { buildSocket(config, gsonParser) }
    private val defaultFileUploader by lazy {
        StreamFileUploader(
            config.apiKey,
            buildRetrofitCdnApi()
        )
    }

    //region Modules

    fun api(): ChatApi {
        return defaultApi
    }

    fun socket(): ChatSocket {
        return defaultSocket
    }

    fun logger(): ChatLogger {
        return defaultLogger
    }

    fun notifications(): ChatNotifications {
        return defaultNotifications
    }

    //endregion

    private fun buildNotification(
        handler: ChatNotificationHandler,
        api: ChatApi,
    ): ChatNotifications {
        return ChatNotifications.create(handler, api, appContext)
    }

    private fun buildRetrofit(
        endpoint: String,
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean,
    ): Retrofit {
        val okHttpClient = clientBuilder(timeout, config, parser, isAnonymousApi).build()

        return Retrofit.Builder()
            .baseUrl(endpoint)
            .client(okHttpClient)
            .also(parser::configRetrofit)
            .addCallAdapterFactory(RetrofitCallAdapterFactory.create(parser))
            .build()
    }

    // Create Builders from a single client to share threadpools
    private val baseClient: OkHttpClient by lazy { OkHttpClient() }
    private fun baseClientBuilder(): OkHttpClient.Builder =
        baseClient.newBuilder().followRedirects(false)

    protected open fun clientBuilder(
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean,
    ): OkHttpClient.Builder {
        return baseClientBuilder()
            // timeouts
            .callTimeout(timeout, TimeUnit.MILLISECONDS)
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            // interceptors
            .addInterceptor(HeadersInterceptor(getAnonymousProvider(config, isAnonymousApi)))
            .addInterceptor(HttpLoggingInterceptor())
            .addInterceptor(
                TokenAuthInterceptor(
                    tokenManager,
                    parser,
                    getAnonymousProvider(config, isAnonymousApi)
                )

            )
            .addInterceptor(
                CurlInterceptor {
                    logger().logI("CURL", it)
                }
            )
    }

    private fun getAnonymousProvider(
        config: ChatClientConfig,
        isAnonymousApi: Boolean,
    ): () -> Boolean {
        return { isAnonymousApi || config.isAnonymous }
    }

    private fun buildSocket(
        chatConfig: ChatClientConfig,
        parser: ChatParser,
    ): ChatSocket {
        return ChatSocketImpl(
            chatConfig.apiKey,
            chatConfig.wssUrl,
            tokenManager,
            parser,
            NetworkStateProvider(appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        )
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun buildApi(chatConfig: ChatClientConfig): ChatApi {
        val gsonChatApi = GsonChatApi(
            chatConfig.apiKey,
            buildRetrofitApi<RetrofitApi>(),
            buildRetrofitApi<RetrofitAnonymousApi>(),
            UuidGeneratorImpl(),
            fileUploader ?: defaultFileUploader
        )

        return if (chatConfig.enableMoshi) {
            MoshiChatApi(
                chatConfig.apiKey,
                gsonChatApi,
                buildRetrofitApi<MessageApi>(),
            )
        } else {
            gsonChatApi
        }
    }

    private inline fun <reified T> buildRetrofitApi(): T {
        val apiClass = T::class.java
        return buildRetrofit(
            config.httpUrl,
            config.baseTimeout,
            config,
            if (apiClass.isMoshiApi) moshiParser else gsonParser,
            apiClass.isAnonymousApi,
        ).create(apiClass)
    }

    private val Class<*>.isMoshiApi: Boolean
        get() = this.annotations.any { it is MoshiApi }

    private val Class<*>.isAnonymousApi: Boolean
        get() {
            val anon = this.annotations.any { it is AnonymousApi }
            val auth = this.annotations.any { it is AuthenticatedApi }

            if (anon && auth) {
                throw IllegalStateException("Api class must be annotated with either @AnonymousApi or @AuthenticatedApi, and not both")
            }

            if (anon) return true
            if (auth) return false

            throw IllegalStateException("Api class must be annotated with either @AnonymousApi or @AuthenticatedApi")
        }

    private fun buildRetrofitCdnApi(): RetrofitCdnApi {
        val apiClass = RetrofitCdnApi::class.java
        return buildRetrofit(
            config.cdnHttpUrl,
            config.cdnTimeout,
            config,
            if (apiClass.isMoshiApi) moshiParser else gsonParser,
            apiClass.isAnonymousApi
        ).create(apiClass)
    }
}
