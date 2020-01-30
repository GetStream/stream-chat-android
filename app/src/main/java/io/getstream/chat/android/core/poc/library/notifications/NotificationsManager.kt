package io.getstream.chat.android.core.poc.library.notifications

import android.content.Context
import io.getstream.chat.android.core.poc.library.events.ChatEvent
import com.google.firebase.messaging.RemoteMessage

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

    //fun setFailMessageListener(failMessageListener: NotificationMessageLoadListener)
}