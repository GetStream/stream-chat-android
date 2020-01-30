package io.getstream.chat.android.core.poc.library.notifications.options

import android.app.PendingIntent
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.core.poc.library.events.ChatEvent

interface NotificationIntentProvider {

    fun getIntentForFirebaseMessage(context: Context, remoteMessage: RemoteMessage): PendingIntent

    fun getIntentForWebSocketEvent(context: Context, event: ChatEvent): PendingIntent
}