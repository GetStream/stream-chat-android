package io.getstream.chat.android.client.notifications

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent

interface NotificationsManager {
    fun setFirebaseToken(
        firebaseToken: String,
        context: Context
    )

    fun onReceiveFirebaseMessage(
        remoteMessage: RemoteMessage,
        context: Context
    )

    fun onReceiveWebSocketEvent(event: ChatEvent, context: Context)

    fun handleRemoteMessage(
        context: Context?,
        remoteMessage: RemoteMessage?
    )

    fun handleEvent(context: Context?, event: ChatEvent?)

    fun setFailMessageListener(failMessageListener: NotificationMessageLoadListener)
}