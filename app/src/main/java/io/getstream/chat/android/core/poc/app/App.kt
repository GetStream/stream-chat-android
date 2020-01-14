package io.getstream.chat.android.core.poc.app

import android.app.Application
import io.getstream.chat.android.core.poc.app.cache.AppDatabase
import io.getstream.chat.android.core.poc.app.common.KeyValue
import io.getstream.chat.android.core.poc.app.repositories.ChannelsRepositoryLive
import io.getstream.chat.android.core.poc.app.repositories.ChannelsRepositoryRx
import io.getstream.chat.android.core.poc.app.repositories.ChannelsRepositorySync
import io.getstream.chat.android.core.poc.library.StreamChatClient
import io.getstream.chat.android.core.poc.library.api.ApiClientOptions

class App : Application() {


    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(this)
        client = StreamChatClient(
            "qk4nn7rpcn75", ApiClientOptions.Builder()
                .baseURL("chat-us-east-1.stream-io-api.com")
                .cdnUrl("chat-us-east-1.stream-io-api.com")
                .timeout(10000)
                .cdnTimeout(10000)
                .build()
        )
        keyValue = KeyValue(this)
        cache = ChannelsCache(db.channels())
        channelsRepositorySync = ChannelsRepositorySync(client, cache)
        channelsRepositoryRx = ChannelsRepositoryRx(client, cache)
        channelsRepositoryLive = ChannelsRepositoryLive(client, cache)
    }

    companion object {
        lateinit var client: StreamChatClient
        lateinit var channelsRepositorySync: ChannelsRepositorySync
        lateinit var channelsRepositoryRx: ChannelsRepositoryRx
        lateinit var channelsRepositoryLive: ChannelsRepositoryLive
        lateinit var db: AppDatabase
        lateinit var cache: ChannelsCache
        lateinit var keyValue: KeyValue
    }
}