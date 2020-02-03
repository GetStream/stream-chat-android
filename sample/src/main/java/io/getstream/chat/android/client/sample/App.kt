package io.getstream.chat.android.client.sample

import android.app.Application
import com.facebook.stetho.Stetho
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatClientBuilder
import io.getstream.chat.android.client.api.ApiClientOptions
import io.getstream.chat.android.client.logger.StreamChatLogger
import io.getstream.chat.android.client.logger.StreamLogger
import io.getstream.chat.android.client.logger.StreamLoggerHandler
import io.getstream.chat.android.client.logger.StreamLoggerLevel
import io.getstream.chat.android.client.sample.cache.AppDatabase
import io.getstream.chat.android.client.sample.common.KeyValue
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositoryLive
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositoryRx
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositorySync

class App : Application() {

    companion object {
        lateinit var client: ChatClient
        lateinit var channelsRepositorySync: ChannelsRepositorySync
        lateinit var channelsRepositoryRx: ChannelsRepositoryRx
        lateinit var channelsRepositoryLive: ChannelsRepositoryLive
        lateinit var db: AppDatabase
        lateinit var cache: ChannelsCache
        lateinit var keyValue: KeyValue
    }

    private lateinit var logger: StreamLogger

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)

        db = AppDatabase.getInstance(this)

        val apiKey = "d2q3juekvgsf"

        val apiOptions = ApiClientOptions.Builder()
            .baseURL("chat-us-east-staging.stream-io-api.com")
            .cdnUrl("chat-us-east-staging.stream-io-api.com")
            .timeout(10000)
            .cdnTimeout(10000)
            .build()

        setupLogger()

        client = ChatClientBuilder(apiKey, apiOptions, logger).build()
        keyValue = KeyValue(this)
        cache = ChannelsCache(db.channels())
        channelsRepositorySync = ChannelsRepositorySync(client, cache)
        channelsRepositoryRx = ChannelsRepositoryRx(client, cache)
        channelsRepositoryLive = ChannelsRepositoryLive(client, cache)
    }

    private fun setupLogger() {
        val loggerHandler: StreamLoggerHandler = object : StreamLoggerHandler {
            override fun logT(throwable: Throwable) {
                // display throwable logs here
            }

            override fun logT(className: String, throwable: Throwable) {
                // display throwable logs here
            }

            override fun logI(className: String, message: String) {
                // display info logs here
            }

            override fun logD(className: String, message: String) {
                // display debug logs here
            }

            override fun logW(className: String, message: String) {
                // display warning logs here
            }

            override fun logE(className: String, message: String) {
                // display error logs here
            }
        }

        logger = StreamChatLogger.Builder()
            .loggingLevel(if (BuildConfig.DEBUG) StreamLoggerLevel.ALL else StreamLoggerLevel.NOTHING)
            .setLoggingHandler(loggerHandler)
            .build()
    }
}