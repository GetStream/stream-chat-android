package io.getstream.chat.android.livedata.service.sync

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

internal class OfflineSyncFirebaseMessagingService : FirebaseMessagingService() {

    private val syncServiceDelegate: StreamOfflineSyncFirebaseServiceDelegate =
        StreamOfflineSyncFirebaseServiceDelegate(this)

    override fun onNewToken(token: String) {
        syncServiceDelegate.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        syncServiceDelegate.onMessageReceived(message)
        stopSelf()
    }
}
