package io.getstream.chat.android.core.poc.library.models

import android.app.PendingIntent
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.core.poc.library.events.ChatEvent

class StreamNotification constructor(
    val notificationId: Int = 0,
    val remoteMessage: RemoteMessage? = null,
    val event: ChatEvent? = null
) {

    var channelName: String? = null
    var messageText: String? = null
    var pendingIntent: PendingIntent? = null
}