package io.getstream.chat.android.core.poc.library.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class StreamFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //StreamChat.getNotificationsManager().onReceiveFirebaseMessage(remoteMessage, this)
    }

    override fun onNewToken(token: String) {
        //StreamChat.getNotificationsManager().setFirebaseToken(token, this)
    }
}