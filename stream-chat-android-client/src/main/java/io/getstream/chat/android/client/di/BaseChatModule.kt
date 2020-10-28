package io.getstream.chat.android.client.di

import android.content.Context
import com.moczul.ok2curl.CurlInterceptor
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.HeadersInterceptor
import io.getstream.chat.android.client.api.HttpLoggingInterceptor
import io.getstream.chat.android.client.api.RetrofitAnonymousApi
import io.getstream.chat.android.client.api.RetrofitApi
import io.getstream.chat.android.client.api.RetrofitCdnApi
import io.getstream.chat.android.client.api.TokenAuthInterceptor
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketImpl
import io.getstream.chat.android.client.uploader.StreamFileUploader
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal open class BaseChatModule(
    private val appContext: Context,
    private val config: ChatClientConfig
) {

    private val defaultLogger = ChatLogger.Builder(config.loggerConfig).build()
    private val defaultParser by lazy { ChatParserImpl() }
    private val defaultNotifications by lazy {
        buildNotification(config.notificationsHandler, api())
    }
    private val defaultApi by lazy { buildApi(config) }
    private val defaultSocket by lazy { buildSocket(config, parser()) }
    private val defaultFileUploader by lazy {
        StreamFileUploader(
            config.apiKey,
            buildRetrofitCdnApi()
        )
    }

    //region Modules

    open fun api(): ChatApi {
        return defaultApi
    }

    open fun socket(): ChatSocket {
        return defaultSocket
    }

    open fun parser(): ChatParser {
        return defaultParser
    }

    open fun logger(): ChatLogger {
        return defaultLogger
    }

    open fun notifications(): ChatNotifications {
        return defaultNotifications
    }

    //endregion

    private fun buildNotification(
        handler: ChatNotificationHandler,
        api: ChatApi
    ): ChatNotifications {
        return ChatNotifications.create(handler, api, appContext)
    }

    private fun buildRetrofit(
        endpoint: String,
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean = false
    ): Retrofit {
        val clientBuilder =
            clientBuilder(connectTimeout, writeTimeout, readTimeout, config, parser, isAnonymousApi)

        val builder = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(clientBuilder.build())

        return parser.configRetrofit(builder).build()
    }

    protected open fun clientBuilder(
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        config: ChatClientConfig,
        parser: ChatParser,
        isAnonymousApi: Boolean
    ): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .followRedirects(false)
            // timeouts
            .callTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            // interceptors
            .addInterceptor(HeadersInterceptor(getAnonymousProvider(config, isAnonymousApi)))
            .addInterceptor(HttpLoggingInterceptor())
            .addInterceptor(
                TokenAuthInterceptor(
                    config.tokenManager,
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
        isAnonymousApi: Boolean
    ): () -> Boolean {
        return { isAnonymousApi || config.isAnonymous }
    }

    private fun buildSocket(
        chatConfig: ChatClientConfig,
        parser: ChatParser
    ): ChatSocket {
        return ChatSocketImpl(
            chatConfig.apiKey,
            chatConfig.wssUrl,
            chatConfig.tokenManager,
            parser
        )
    }

    private fun buildApi(chatConfig: ChatClientConfig): ChatApi {
        return ChatApi(
            chatConfig.apiKey,
            buildRetrofitApi(RetrofitApi::class.java, false),
            buildRetrofitApi(RetrofitAnonymousApi::class.java, true),
            UuidGeneratorImpl(),
            chatConfig.fileUploader ?: defaultFileUploader
        )
    }

    private fun <T> buildRetrofitApi(apiClass: Class<T>, isAnonymousApi: Boolean): T {
        return buildRetrofit(
            config.httpUrl,
            config.baseTimeout,
            config.baseTimeout,
            config.baseTimeout,
            config,
            parser(),
            isAnonymousApi
        ).create(apiClass)
    }

    private fun buildRetrofitCdnApi(): RetrofitCdnApi {
        return buildRetrofit(
            config.cdnHttpUrl,
            config.cdnTimeout,
            config.cdnTimeout,
            config.cdnTimeout,
            config,
            parser()
        ).create(RetrofitCdnApi::class.java)
    }
}
