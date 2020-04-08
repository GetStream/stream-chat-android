package io.getstream.chat.android.client.notifications

import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent

interface ChatNotifications {

    fun onSetUser()

    fun setFirebaseToken(firebaseToken: String)

    fun onFirebaseMessage(message: RemoteMessage)

    fun onChatEvent(event: ChatEvent)
}