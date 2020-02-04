package io.getstream.chat.android.client.sample

import android.app.Application
import com.facebook.stetho.Stetho
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatConfig
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

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)

        db = AppDatabase.getInstance(this)

        val config = ChatConfig.Builder()
            .apiKey("qk4nn7rpcn75")
            .baseURL("chat-us-east-staging.stream-io-api.com")
            .cdnUrl("chat-us-east-staging.stream-io-api.com")
            .baseTimeout(10000)
            .cdnTimeout(10000)
            .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
            .build()

        client = ChatClient.init(
            ChatClient.Builder()
                .config(config)
                .logger(initLogger())
        )

        keyValue = KeyValue(this)
        cache = ChannelsCache(db.channels())
        channelsRepositorySync = ChannelsRepositorySync(client, cache)
        channelsRepositoryRx = ChannelsRepositoryRx(client, cache)
        channelsRepositoryLive = ChannelsRepositoryLive(client, cache)
    }

    private fun initLogger(): StreamLogger {
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

        return StreamChatLogger.Builder()
            .loggingLevel(if (BuildConfig.DEBUG) StreamLoggerLevel.ALL else StreamLoggerLevel.NOTHING)
            .setLoggingHandler(loggerHandler)
            .build()
    }
}