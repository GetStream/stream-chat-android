package io.getstream.chat.android.client.notifications.options

import android.app.PendingIntent
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent

interface NotificationIntentProvider {

    fun getIntentForFirebaseMessage(context: Context, remoteMessage: RemoteMessage): PendingIntent

    fun getIntentForWebSocketEvent(context: Context, event: ChatEvent): PendingIntent
}