package io.getstream.chat.android.client.di

import android.content.Context
import android.net.ConnectivityManager
import com.moczul.ok2curl.CurlInterceptor
import io.getstream.chat.android.client.api.AnonymousApi
import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.RetrofitCallAdapterFactory
import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.interceptor.ApiKeyInterceptor
import io.getstream.chat.android.client.api.interceptor.HeadersInterceptor
import io.getstream.chat.android.client.api.interceptor.HttpLoggingInterceptor
import io.getstream.chat.android.client.api.interceptor.ProgressInterceptor
import io.getstream.chat.android.client.api.interceptor.TokenAuthInterceptor
import io.getstream.chat.android.client.api2.ChannelApi
import io.getstream.chat.android.client.api2.DeviceApi
import io.getstream.chat.android.client.api2.GeneralApi
import io.getstream.chat.android.client.api2.GuestApi
import io.getstream.chat.android.client.api2.MessageApi
import io.getstream.chat.android.client.api2.ModerationApi
import io.getstream.chat.android.client.api2.MoshiChatApi
import io.getstream.chat.android.client.api2.UserApi
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.helpers.QueryChannelsPostponeHelper
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.ChatNotificationsImpl
import io.getstream.chat.android.client.notifications.NoOpChatNotifications
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.MoshiChatParser
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketImpl
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.StreamFileUploader
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

internal open class BaseChatModule(
    private val appContext: Context,
    private val config: ChatClientConfig,
    private val notificationsHandler: NotificationHandler,
    private val fileUploader: FileUploader? = null,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val callbackExecutor: Executor?,
    private val customOkHttpClient: OkHttpClient? = null,
    private val httpClientConfig: (OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
) {

    private val defaultLogger: ChatLogger = ChatLogger.Builder(config.loggerConfig).build()

    private val moshiParser: ChatParser by lazy { MoshiChatParser() }

    private val defaultNotifications by lazy { buildNotification(notificationsHandler) }
    private val defaultApi by lazy { buildApi() }
    private val defaultSocket by lazy {
        buildSocket(config, moshiParser)
    }
    private val defaultFileUploader by lazy {
        StreamFileUploader(buildRetrofitCdnApi())
    }

    private val networkScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO)
    val socketStateService: SocketStateService = SocketStateService()
    val userStateService: UserStateService = UserStateService()
    val queryChannelsPostponeHelper: QueryChannelsPostponeHelper by lazy {
        QueryChannelsPostponeHelper(
            api(),
            socketStateService,
            networkScope,
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
        handler: NotificationHandler,
    ): ChatNotifications {
        return if (handler.config.pushNotificationsEnabled) {
            ChatNotificationsImpl(handler, appContext)
        } else {
            NoOpChatNotifications(handler)
        }
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
            .addCallAdapterFactory(RetrofitCallAdapterFactory.create(parser, callbackExecutor))
            .build()
    }

    // Create Builders from a single client to share threadpools
    private val baseClient: OkHttpClient by lazy { customOkHttpClient ?: OkHttpClient() }
    private fun baseClientBuilder(): OkHttpClient.Builder =
        baseClient.newBuilder().followRedirects(false)

    protected open fun clientBuilder(
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean,
    ): OkHttpClient.Builder {
        return baseClientBuilder()
            .apply {
                if (baseClient != customOkHttpClient) {
                    connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    readTimeout(timeout, TimeUnit.MILLISECONDS)
                }
            }
            // timeouts

            // interceptors
            .addInterceptor(ApiKeyInterceptor(config.apiKey))
            .addInterceptor(HeadersInterceptor(getAnonymousProvider(config, isAnonymousApi)))
            .let(httpClientConfig)
            .addInterceptor(
                TokenAuthInterceptor(
                    tokenManager,
                    parser,
                    getAnonymousProvider(config, isAnonymousApi)
                )
            )
            .apply {
                if (config.loggerConfig.level != ChatLogLevel.NOTHING) {
                    addInterceptor(HttpLoggingInterceptor())
                    addInterceptor(
                        CurlInterceptor { message ->
                            logger().logI("CURL", message)
                        }
                    )
                }
            }
            .addNetworkInterceptor(ProgressInterceptor())
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
            NetworkStateProvider(appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager),
            networkScope,
        )
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun buildApi(): ChatApi {
        return MoshiChatApi(
            fileUploader ?: defaultFileUploader,
            buildRetrofitApi<UserApi>(),
            buildRetrofitApi<GuestApi>(),
            buildRetrofitApi<MessageApi>(),
            buildRetrofitApi<ChannelApi>(),
            buildRetrofitApi<DeviceApi>(),
            buildRetrofitApi<ModerationApi>(),
            buildRetrofitApi<GeneralApi>(),
            networkScope,
        )
    }

    private inline fun <reified T> buildRetrofitApi(): T {
        val apiClass = T::class.java
        return buildRetrofit(
            config.httpUrl,
            config.baseTimeout,
            config,
            moshiParser,
            apiClass.isAnonymousApi,
        ).create(apiClass)
    }

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
            moshiParser,
            apiClass.isAnonymousApi,
        ).create(apiClass)
    }
}
