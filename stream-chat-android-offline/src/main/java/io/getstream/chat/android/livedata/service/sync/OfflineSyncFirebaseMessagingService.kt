package io.getstream.chat.android.livedata.service.sync

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

internal class OfflineSyncFirebaseMessagingService : FirebaseMessagingService() {

    private val pushDataSyncHandler: PushMessageSyncHandler =
        PushMessageSyncHandler(this)

    override fun onNewToken(token: String) {
        pushDataSyncHandler.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        pushDataSyncHandler.onMessageReceived(message)
        stopSelf()
    }
}
