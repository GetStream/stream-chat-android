package io.getstream.chat.android.livedata.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.ChatNotificationConfigOffline.Companion.EXTRA_CID

class SyncService: Service() {

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        NotificationCompat.Builder(this, CHANNEL_ID)
            // TODO: make titles dynamic
            .setContentText("Sync notification title")
            .setContentText("Sync notification content")
            .setAutoCancel(true)
            .build()
            .apply {
                startForeground(1, this)
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            intent?.getStringExtra(EXTRA_CID)?.let {
                // TODO: ChatDomain is null when app gets killed.
                //  Need to store api key to re-create ChatClient + ChatDomain here.
                ChatDomain.instance().apply {
                    useCases.replayEventsForActiveChannels(it).execute()
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
    }
}