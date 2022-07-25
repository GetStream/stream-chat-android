/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.Lifecycle
import com.moczul.ok2curl.CurlInterceptor
import io.getstream.chat.android.client.api.AnonymousApi
import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.RetrofitCallAdapterFactory
import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.interceptor.ApiKeyInterceptor
import io.getstream.chat.android.client.api.interceptor.ApiRequestAnalyserInterceptor
import io.getstream.chat.android.client.api.interceptor.HeadersInterceptor
import io.getstream.chat.android.client.api.interceptor.HttpLoggingInterceptor
import io.getstream.chat.android.client.api.interceptor.ProgressInterceptor
import io.getstream.chat.android.client.api.interceptor.TokenAuthInterceptor
import io.getstream.chat.android.client.api.internal.DistinctChatApi
import io.getstream.chat.android.client.api.internal.DistinctChatApiEnabler
import io.getstream.chat.android.client.api.internal.ExtraDataValidator
import io.getstream.chat.android.client.api2.MoshiChatApi
import io.getstream.chat.android.client.api2.endpoint.ChannelApi
import io.getstream.chat.android.client.api2.endpoint.ConfigApi
import io.getstream.chat.android.client.api2.endpoint.DeviceApi
import io.getstream.chat.android.client.api2.endpoint.GeneralApi
import io.getstream.chat.android.client.api2.endpoint.GuestApi
import io.getstream.chat.android.client.api2.endpoint.MessageApi
import io.getstream.chat.android.client.api2.endpoint.ModerationApi
import io.getstream.chat.android.client.api2.endpoint.UserApi
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.experimental.socket.lifecycle.NetworkLifecyclePublisher
import io.getstream.chat.android.client.experimental.socket.lifecycle.StreamLifecyclePublisher
import io.getstream.chat.android.client.helpers.CallPostponeHelper
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.ChatNotificationsImpl
import io.getstream.chat.android.client.notifications.NoOpChatNotifications
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.MoshiChatParser
import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.StreamFileUploader
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import io.getstream.chat.android.client.experimental.socket.ChatSocket as ChatSocketExperimental

@Suppress("TooManyFunctions")
internal open class BaseChatModule(
    private val appContext: Context,
    private val config: ChatClientConfig,
    private val notificationsHandler: NotificationHandler,
    private val notificationConfig: NotificationConfig,
    private val fileUploader: FileUploader? = null,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val customOkHttpClient: OkHttpClient? = null,
    private val lifecycle: Lifecycle,
    private val httpClientConfig: (OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
) {

    private val defaultLogger: ChatLogger = ChatLogger.Builder(config.loggerConfig).build()

    private val moshiParser: ChatParser by lazy { MoshiChatParser() }

    private val defaultNotifications by lazy { buildNotification(notificationsHandler, notificationConfig) }
    private val defaultApi by lazy { buildApi(config) }
    private val defaultSocket by lazy {
        buildSocket(config, moshiParser)
    }
    private val chatSocketExperimental: ChatSocketExperimental by lazy {
        buildExperimentalChatSocket(config, moshiParser)
    }
    private val defaultFileUploader by lazy {
        StreamFileUploader(buildRetrofitCdnApi())
    }

    val networkScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO)
    val socketStateService: SocketStateService = SocketStateService()
    val userStateService: UserStateService = UserStateService()
    val callPostponeHelper: CallPostponeHelper by lazy {
        CallPostponeHelper(
            socketStateService = socketStateService,
            coroutineScope = networkScope,
            chatSocketExperimental = chatSocketExperimental
        )
    }

    //region Modules

    fun api(): ChatApi = defaultApi

    fun socket(): ChatSocket = defaultSocket

    fun experimentalSocket() = chatSocketExperimental

    fun networkLifecyclePublisher(): NetworkLifecyclePublisher =
        NetworkLifecyclePublisher(appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    @Deprecated(
        message = "Use StreamLog instead.",
        replaceWith = ReplaceWith(
            expression = "StreamLog",
            imports = ["io.getstream.logging.StreamLog"]
        ),
        level = DeprecationLevel.WARNING
    )
    fun logger(): ChatLogger = defaultLogger

    fun notifications(): ChatNotifications = defaultNotifications

    //endregion

    private fun buildNotification(
        handler: NotificationHandler,
        notificationConfig: NotificationConfig,
    ): ChatNotifications = if (notificationConfig.pushNotificationsEnabled) {
        ChatNotificationsImpl(handler, notificationConfig, appContext)
    } else {
        NoOpChatNotifications
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
            .addCallAdapterFactory(RetrofitCallAdapterFactory.create(parser, networkScope))
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
            .apply {
                if (config.debugRequests) {
                    addInterceptor(ApiRequestAnalyserInterceptor(ApiRequestsAnalyser.get()))
                }
            }
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
                            StreamLog.i("CURL") { message }
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
    ) = ChatSocket(
        chatConfig.apiKey,
        chatConfig.wssUrl,
        tokenManager,
        SocketFactory(parser, tokenManager),
        NetworkStateProvider(appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager),
        parser,
        networkScope,
    )

    private fun buildExperimentalChatSocket(
        chatConfig: ChatClientConfig,
        parser: ChatParser,
    ) = ChatSocketExperimental(
        chatConfig.apiKey,
        chatConfig.wssUrl,
        tokenManager,
        SocketFactory(parser, tokenManager),
        networkScope,
        parser,
        listOf(
            StreamLifecyclePublisher(lifecycle),
            networkLifecyclePublisher(),
        ),
    )

    @Suppress("RemoveExplicitTypeArguments")
    private fun buildApi(chatConfig: ChatClientConfig): ChatApi = MoshiChatApi(
        fileUploader ?: defaultFileUploader,
        buildRetrofitApi<UserApi>(),
        buildRetrofitApi<GuestApi>(),
        buildRetrofitApi<MessageApi>(),
        buildRetrofitApi<ChannelApi>(),
        buildRetrofitApi<DeviceApi>(),
        buildRetrofitApi<ModerationApi>(),
        buildRetrofitApi<GeneralApi>(),
        buildRetrofitApi<ConfigApi>(),
        networkScope,
    ).let { originalApi ->
        DistinctChatApiEnabler(DistinctChatApi(networkScope, originalApi)) {
            chatConfig.distinctApiCalls
        }
    }.let { originalApi ->
        ExtraDataValidator(networkScope, originalApi)
    }

    private inline fun <reified T> buildRetrofitApi(): T {
        val apiClass = T::class.java
        return buildRetrofit(
            config.httpUrl,
            BASE_TIMEOUT,
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
                throw IllegalStateException(
                    "Api class must be annotated with either @AnonymousApi or @AuthenticatedApi, and not both"
                )
            }

            if (anon) return true
            if (auth) return false

            throw IllegalStateException("Api class must be annotated with either @AnonymousApi or @AuthenticatedApi")
        }

    private fun buildRetrofitCdnApi(): RetrofitCdnApi {
        val apiClass = RetrofitCdnApi::class.java
        return buildRetrofit(
            config.cdnHttpUrl,
            CDN_TIMEOUT,
            config,
            moshiParser,
            apiClass.isAnonymousApi,
        ).create(apiClass)
    }

    private companion object {
        private const val BASE_TIMEOUT = 30_000L
        private var CDN_TIMEOUT = 30_000L
    }
}
