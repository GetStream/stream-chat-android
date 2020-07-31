package io.getstream.chat.android.livedata.service.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

class SyncService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        showForegroundNotification()

        intent?.getStringExtra(EXTRA_CID)?.let {
            val config = EncryptedBackgroundSyncConfigStore(applicationContext).get()
            val user = User(id = config.userId)
            val chatClient = ChatClient.Builder(config.apiKey, applicationContext).build()
            ChatDomain.Builder(applicationContext, chatClient, user).build().apply {
                val result = useCases.replayEventsForActiveChannels(it).execute()
                if (result.isSuccess) {
                    val numberOfNewMessages =
                        result.data().filterIsInstance<NewMessageEvent>().count()
                    // TODO: display notification about X new messages
                    Log.e("SyncService", "sync success $numberOfNewMessages new messages")
                } else {
                    // TODO: In case sync failed display generic notification that there are a new messages
                }
            }
        }

        stopForeground(true)
        stopSelf()
        return START_NOT_STICKY
    }

    private fun showForegroundNotification() {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(true)
            .build()
            .apply {
                startForeground(NOTIFICATION_ID, this)
            }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).run {
                getSystemService(NotificationManager::class.java).createNotificationChannel(this)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "notification_channel_id"
        const val CHANNEL_NAME = "Chat messages sync"
        const val NOTIFICATION_ID = 1
        const val EXTRA_CID = "key_channel_cid"

        fun createIntent(context: Context, cid: String) =
            Intent(context, SyncService::class.java).apply {
                putExtra(EXTRA_CID, cid)
            }
    }
}
