package io.getstream.chat.android.livedata

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMessageNew
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.livedata.worker.SyncWorker

/**
 * Extend the ChatNotificationConfig to start the sync worker when receiving new messages
 * This ensures that offline storage is up to date as soon as you open the app
 */
class ChatNotificationConfigOffline(context: Context) : ChatNotificationConfig(context) {
    override fun onChatEvent(event: ChatEvent): Boolean {
        when (event) {
            is NewMessageEvent, is NotificationMessageNew, is ReactionNewEvent -> {
                //  Start background work manager
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val data = workDataOf("STREAM_CHANNEL_CID" to event.cid, "STREAM_USER_ID" to ChatDomain.instance().currentUser.id)
                val request = OneTimeWorkRequestBuilder<SyncWorker>().setInputData(data).setConstraints(constraints)
                    .build()
                val operation = WorkManager.getInstance(context).enqueue(request)
            }
        }
        return false
    }
}
