package io.getstream.chat.android.client.models

import android.app.PendingIntent
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent

class ChatNotification constructor(
    val notificationId: Int = 0,
    val remoteMessage: RemoteMessage? = null,
    val event: ChatEvent? = null
) {

    var channelName: String? = null
    var messageText: String? = null
    var pendingReplyIntent: PendingIntent? = null
    var pendingReadIntent: PendingIntent? = null
}