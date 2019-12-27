package io.getstream.chat.android.core.poc.app

import android.app.Application
import io.getstream.chat.android.core.poc.app.cache.AppDatabase
import io.getstream.chat.android.core.poc.app.common.KeyValue
import io.getstream.chat.android.core.poc.app.repositories.ChannelsRepositoryLive
import io.getstream.chat.android.core.poc.app.repositories.ChannelsRepositoryRx
import io.getstream.chat.android.core.poc.app.repositories.ChannelsRepositorySync
import io.getstream.chat.android.core.poc.library.StreamChatClient

class App : Application() {


    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(this)
        client = StreamChatClient()
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