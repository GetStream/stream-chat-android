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
import com.moczul.ok2curl.logger.Logger
import io.getstream.chat.android.client.StreamLifecycleObserver
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
import io.getstream.chat.android.client.api2.endpoint.FileDownloadApi
import io.getstream.chat.android.client.api2.endpoint.GeneralApi
import io.getstream.chat.android.client.api2.endpoint.GuestApi
import io.getstream.chat.android.client.api2.endpoint.MessageApi
import io.getstream.chat.android.client.api2.endpoint.ModerationApi
import io.getstream.chat.android.client.api2.endpoint.OpenGraphApi
import io.getstream.chat.android.client.api2.endpoint.PollsApi
import io.getstream.chat.android.client.api2.endpoint.ThreadsApi
import io.getstream.chat.android.client.api2.endpoint.UserApi
import io.getstream.chat.android.client.api2.endpoint.VideoCallApi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.debugger.ChatClientDebugger
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.ChatNotificationsImpl
import io.getstream.chat.android.client.notifications.NoOpChatNotifications
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.MoshiChatParser
import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import io.getstream.chat.android.client.scope.ClientScope
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.token.TokenManagerImpl
import io.getstream.chat.android.client.uploader.FileTransformer
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.client.uploader.StreamFileUploader
import io.getstream.chat.android.client.user.CurrentUserFetcher
import io.getstream.chat.android.client.transformer.ApiModelTransformers
import io.getstream.chat.android.models.UserId
import io.getstream.log.StreamLog
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
internal open class BaseChatModule
@Suppress("LongParameterList")
constructor(
    private val appContext: Context,
    private val clientScope: ClientScope,
    private val userScope: UserScope,
    private val config: ChatClientConfig,
    private val notificationsHandler: NotificationHandler,
    private val apiModelTransformers: ApiModelTransformers,
    private val fileTransformer: FileTransformer,
    private val fileUploader: FileUploader? = null,
    private val tokenManager: TokenManager = TokenManagerImpl(),
    private val customOkHttpClient: OkHttpClient? = null,
    private val clientDebugger: ChatClientDebugger? = null,
    private val lifecycle: Lifecycle,
    private val httpClientConfig: (OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
) {

    private val domainMapping by lazy {
        DomainMapping(
            currentUserIdProvider,
            apiModelTransformers.incomingChannelTransformer,
            apiModelTransformers.incomingMessageTransformer,
            apiModelTransformers.incomingUserTransformer,
        )
    }
    internal val dtoMapping by lazy {
        DtoMapping(
            apiModelTransformers.outgoingMessageTransformer,
            apiModelTransformers.outgoingUserTransformers,
        )
    }
    private val eventMapping by lazy { EventMapping(domainMapping) }

    private val moshiParser: ChatParser by lazy {
        MoshiChatParser(
            eventMapping = eventMapping,
            dtoMapping = dtoMapping,
        )
    }
    private val socketFactory: SocketFactory by lazy { SocketFactory(moshiParser, tokenManager) }

    private val defaultNotifications by lazy { buildNotification(notificationsHandler, config.notificationConfig) }
    private val defaultApi by lazy { buildApi(config) }
    internal val chatSocket: ChatSocket by lazy { buildChatSocket(config) }
    private val defaultFileUploader by lazy {
        StreamFileUploader(buildRetrofitCdnApi())
    }

    val lifecycleObserver: StreamLifecycleObserver by lazy { StreamLifecycleObserver(userScope, lifecycle) }
    val networkStateProvider: NetworkStateProvider by lazy {
        NetworkStateProvider(userScope, appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
    }
    val userStateService: UserStateService = UserStateService()

    val mutableClientState by lazy {
        MutableClientState(networkStateProvider)
    }

    val currentUserFetcher by lazy {
        CurrentUserFetcher(
            networkStateProvider = networkStateProvider,
            socketFactory = socketFactory,
            config = config,
        )
    }
    private val currentUserIdProvider: () -> UserId? = { userScope.userId.value }

    //region Modules

    fun api(): ChatApi = defaultApi

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
            .addCallAdapterFactory(RetrofitCallAdapterFactory.create(parser, userScope))
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
            .addInterceptor(HeadersInterceptor(context = appContext, getAnonymousProvider(config, isAnonymousApi)))
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
                    getAnonymousProvider(config, isAnonymousApi),
                ),
            )
            .apply {
                if (config.loggerConfig.level != ChatLogLevel.NOTHING) {
                    addInterceptor(HttpLoggingInterceptor())
                    addInterceptor(
                        CurlInterceptor(
                            logger = object : Logger {
                                override fun log(message: String) {
                                    StreamLog.i("Chat:CURL") { message }
                                }
                            },
                        ),
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

    private fun buildChatSocket(
        chatConfig: ChatClientConfig,
    ) = ChatSocket(
        chatConfig.apiKey,
        chatConfig.wssUrl,
        tokenManager,
        socketFactory,
        userScope,
        lifecycleObserver,
        networkStateProvider,
        clientDebugger,
    )

    @Suppress("RemoveExplicitTypeArguments")
    private fun buildApi(chatConfig: ChatClientConfig): ChatApi = MoshiChatApi(
        domainMapping = domainMapping,
        eventMapping = eventMapping,
        dtoMapping = dtoMapping,
        fileUploader ?: defaultFileUploader,
        fileTransformer = fileTransformer,
        buildRetrofitApi<UserApi>(),
        buildRetrofitApi<GuestApi>(),
        buildRetrofitApi<MessageApi>(),
        buildRetrofitApi<ChannelApi>(),
        buildRetrofitApi<DeviceApi>(),
        buildRetrofitApi<ModerationApi>(),
        buildRetrofitApi<GeneralApi>(),
        buildRetrofitApi<ConfigApi>(),
        buildRetrofitApi<VideoCallApi>(),
        buildRetrofitApi<FileDownloadApi>(),
        buildRetrofitApi<OpenGraphApi>(),
        buildRetrofitApi<ThreadsApi>(),
        buildRetrofitApi<PollsApi>(),
        userScope,
        userScope,
    ).let { originalApi ->
        DistinctChatApiEnabler(DistinctChatApi(userScope, originalApi)) {
            chatConfig.distinctApiCalls
        }
    }.let { originalApi ->
        ExtraDataValidator(userScope, originalApi)
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
                    "Api class must be annotated with either @AnonymousApi or @AuthenticatedApi, and not both",
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
