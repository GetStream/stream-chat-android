package io.getstream.chat.android.client

import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Loggable
import io.getstream.chat.android.client.api.*
import io.getstream.chat.android.client.bitmaps.BitmapsLoaderImpl
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.ChatNotificationsImpl
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketImpl
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal open class ChatModules(val config: ChatClientConfig) {

    private val defaultLogger = ChatLogger.Builder(config.loggerConfig).build()
    private val defaultParser by lazy { ChatParserImpl() }
    private val defaultNotifications by lazy { buildNotification(config.notificationsConfig, api()) }
    private val defaultApi by lazy { buildApi(config, parser()) }
    private val defaultSocket by lazy { buildSocket(config, parser()) }
    private val bitmapsLoader = BitmapsLoaderImpl(config.notificationsConfig.context)

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
        config: ChatNotificationConfig,
        api: ChatApi
    ): ChatNotifications {
        return ChatNotificationsImpl(config, api, config.context)
    }

    private fun buildRetrofit(
        endpoint: String,
        connectTimeout: Long,
        writeTimeout: Long,
        readTimeout: Long,
        config: ChatClientConfig,
        parser: ChatParser
    ): Retrofit {

        val clientBuilder = OkHttpClient.Builder()
            .followRedirects(false)
            // timeouts
            .callTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            // interceptors
            .addInterceptor(HeadersInterceptor(config))
            .addInterceptor(HttpLoggingInterceptor())
            .addInterceptor(TokenAuthInterceptor(
                config.tokenManager,
                parser
            ) { config.isAnonymous })
            .addInterceptor(CurlInterceptor(Loggable {
                logger().logI("CURL", it)
            }))

        val builder = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(clientBuilder.build())

        return parser.configRetrofit(builder).build()
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

    private fun buildApi(
        chatConfig: ChatClientConfig,
        parser: ChatParser
    ): ChatApi {
        return ChatApiImpl(
            chatConfig.apiKey,
            buildRetrofitApi(),
            buildRetrofitCdnApi(),
            parser,
            UuidGeneratorImpl()
        )
    }

    private fun buildRetrofitApi(): RetrofitApi {
        return buildRetrofit(
            config.httpUrl,
            config.baseTimeout,
            config.baseTimeout,
            config.baseTimeout,
            config,
            parser()
        ).create(RetrofitApi::class.java)
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