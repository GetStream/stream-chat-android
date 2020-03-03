package io.getstream.chat.android.client.sample

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.sample.cache.AppDatabase
import io.getstream.chat.android.client.sample.common.HomeActivity
import io.getstream.chat.android.client.sample.common.KeyValue
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositoryLive
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositoryRx
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositorySync
import kotlin.time.ExperimentalTime

class App : Application() {

    companion object {
        lateinit var client: ChatClient
        lateinit var channelsRepositorySync: ChannelsRepositorySync
        lateinit var channelsRepositoryRx: ChannelsRepositoryRx
        lateinit var channelsRepositoryLive: ChannelsRepositoryLive
        lateinit var db: AppDatabase
        lateinit var cache: ChannelsCache
        lateinit var keyValue: KeyValue

        val CHANNEL_TYPE_KEY = "type"
        val CHANNEL_ID_KEY = "id"

        private const val EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE"
        private const val EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID"
    }

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)

        FirebaseApp.initializeApp(this)

        db = AppDatabase.getInstance(this)

        val apiKey = "qk4nn7rpcn75"
        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"

        client = ChatClient.Builder(apiKey, token, this)
            .notifications(provideNotificationConfig())
            .logLevel(if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING)
            .build()

        keyValue = KeyValue(this)
        cache = ChannelsCache(db.channels())
        channelsRepositorySync = ChannelsRepositorySync(client, cache)
        channelsRepositoryRx = ChannelsRepositoryRx(client, cache)
        channelsRepositoryLive = ChannelsRepositoryLive(client, cache)
    }


    @UseExperimental(ExperimentalTime::class)
    private fun provideNotificationConfig() = object : ChatNotificationConfig(this) {

        override fun getIntentForFirebaseMessage(
            remoteMessage: RemoteMessage
        ): PendingIntent {
            val payload = remoteMessage.data
            val intent = Intent(context, HomeActivity::class.java)
            intent.apply {
                putExtra(
                    EXTRA_CHANNEL_TYPE,
                    payload[CHANNEL_TYPE_KEY]
                )
                putExtra(
                    EXTRA_CHANNEL_ID,
                    payload[CHANNEL_ID_KEY]
                )
            }
            return PendingIntent.getActivity(
                context, getRequestCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        override fun getIntentForSocketEvent(
            event: ChatEvent
        ): PendingIntent {
            val intent = Intent(context, HomeActivity::class.java)
            intent.apply {
                putExtra(EXTRA_CHANNEL_TYPE, event.message.type)
                putExtra(EXTRA_CHANNEL_ID, event.message.id)
            }
            return PendingIntent.getActivity(
                context, getRequestCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    }
}