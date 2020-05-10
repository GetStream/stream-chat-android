package io.getstream.chat.android.livedata

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.livedata.worker.SyncWorker

/**
 * Extend the ChatNotificationConfig to start the sync worker when receiving new messages
 * This ensures that offline storage is up to date as soon as you open the app
 */
open class ChatNotificationConfigOffline(context: Context) : ChatNotificationConfig(context) {
    private val logger = ChatLogger.get("ChatDomain ChatNotificationConfigOffline")

    override fun onFirebaseMessage(message: RemoteMessage): Boolean {
        logger.logI("onFirebaseMessage received a message")

        val data = message.getData()

        val channelId = data.get("channel_id").toString()
        val channelType = data.get("channel_type").toString()
        val cid = "$channelType:$channelId"

        logger.logI("onFirebaseMessage started a worker to update channel $cid")
        //  Start background work manager
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        // TODO: how does this work when the app is backgrounded?

        val taskData = workDataOf("STREAM_CHANNEL_CID" to cid, "STREAM_USER_ID" to ChatDomain.instance().currentUser.id)
        val request = OneTimeWorkRequestBuilder<SyncWorker>().setInputData(taskData).setConstraints(constraints)
                .build()
        WorkManager.getInstance(context).enqueue(request)

        return super.onFirebaseMessage(message)
    }
}
