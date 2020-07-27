package io.getstream.chat.android.livedata.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.livedata.ChatDomain

class SyncService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(true)
            .build()
            .apply {
                startForeground(NOTIFICATION_ID, this)
            }

        try {
            intent?.getStringExtra(EXTRA_CID)?.let {
                // TODO: ChatDomain is null when app gets killed.
                // That's why it's wrapped in try clause atm.
                // Need to store api key to re-create ChatClient + ChatDomain here.
                ChatDomain.instance().apply {
                    val result = useCases.replayEventsForActiveChannels(it).execute()
                    if (result.isSuccess) {
                        val numberOfNewMessages =
                            result.data().filterIsInstance<NewMessageEvent>().count()
                        // TODO: display notification about X new messages
                    } else {
                        // TODO: In case sync failed display generic notification that there are a new messages
                    }
                }
            }
        } finally {
            stopForeground(true)
            stopSelf()
        }
        return START_NOT_STICKY
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