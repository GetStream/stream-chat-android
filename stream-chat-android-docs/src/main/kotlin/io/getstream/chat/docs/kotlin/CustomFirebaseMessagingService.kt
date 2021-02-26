package io.getstream.chat.docs.kotlin

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.livedata.service.sync.PushMessageSyncHandler

/**
 * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin">Handling notifications from multiple backend services</a>
 */
class CustomFirebaseMessagingService : FirebaseMessagingService() {
    private val pushDataSyncHandler: PushMessageSyncHandler =
        PushMessageSyncHandler(this)

    override fun onNewToken(token: String) {
        // update device's token on Stream backend
        pushDataSyncHandler.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (pushDataSyncHandler.isStreamMessage(message)) {
            // handle RemoteMessage sent from Stream backend
            pushDataSyncHandler.onMessageReceived(message)
        } else {
            // handle RemoteMessage from other source
        }
        stopSelf()
    }
}
