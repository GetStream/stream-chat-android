package io.getstream.chat.android.client.di

import android.content.Context
import com.moczul.ok2curl.CurlInterceptor
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.HeadersInterceptor
import io.getstream.chat.android.client.api.HttpLoggingInterceptor
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

    private val defaultLogger: ChatLogger = ChatLogger.Builder(config.loggerConfig).build()
    private val defaultParser: ChatParser by lazy { ChatParserImpl() }
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

    fun api(): ChatApi {
        return defaultApi
    }

    fun socket(): ChatSocket {
        return defaultSocket
    }

    fun parser(): ChatParser {
        return defaultParser
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
        api: ChatApi
    ): ChatNotifications {
        return ChatNotifications.create(handler, api, appContext)
    }

    private fun buildRetrofit(
        endpoint: String,
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser
    ): Retrofit {
        val okHttpClient = clientBuilder(timeout, config, parser).build()

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(endpoint)
            .client(okHttpClient)

        return parser.configRetrofit(retrofitBuilder).build()
    }

    protected open fun clientBuilder(
        timeout: Long,
        config: ChatClientConfig,
        parser: ChatParser
    ): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .followRedirects(false)
            // timeouts
            .callTimeout(timeout, TimeUnit.MILLISECONDS)
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            // interceptors
            .addInterceptor(HeadersInterceptor(config))
            .addInterceptor(HttpLoggingInterceptor())
            .addInterceptor(
                TokenAuthInterceptor(
                    config.tokenManager,
                    parser
                ) { config.isAnonymous }
            )
            .addInterceptor(
                CurlInterceptor {
                    logger().logI("CURL", it)
                }
            )
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
            buildRetrofitApi(),
            UuidGeneratorImpl(),
            chatConfig.fileUploader ?: defaultFileUploader
        )
    }

    private fun buildRetrofitApi(): RetrofitApi {
        return buildRetrofit(
            config.httpUrl,
            config.baseTimeout,
            config,
            parser()
        ).create(RetrofitApi::class.java)
    }

    private fun buildRetrofitCdnApi(): RetrofitCdnApi {
        return buildRetrofit(
            config.cdnHttpUrl,
            config.cdnTimeout,
            config,
            parser()
        ).create(RetrofitCdnApi::class.java)
    }
}
