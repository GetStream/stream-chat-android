package io.getstream.chat.android.client.models

import android.app.PendingIntent
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent

class NotificationData constructor(
    val remoteMessage: RemoteMessage? = null,
    val event: ChatEvent? = null
) {

    val notificationTimestamp: Int = System.currentTimeMillis().toInt()

//    var channelName: String? = null
//    var messageText: String? = null
//    var pendingReplyIntent: PendingIntent? = null
//    var pendingReadIntent: PendingIntent? = null
}