package io.getstream.chat.android.livedata

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.livedata.service.SyncService

/**
 * Extend the ChatNotificationConfig to start the sync service when receiving new messages
 * This ensures that offline storage is up to date as soon as you open the app
 */
open class ChatNotificationConfigOffline(context: Context) : ChatNotificationConfig(context) {

    override fun onFirebaseMessage(message: RemoteMessage): Boolean {
        val data = message.data
        val channelId = data.get("channel_id").toString()
        val channelType = data.get("channel_type").toString()
        val cid = "$channelType:$channelId"

        ContextCompat.startForegroundService(context, SyncService.createIntent(context, cid))

        return true
    }

}
