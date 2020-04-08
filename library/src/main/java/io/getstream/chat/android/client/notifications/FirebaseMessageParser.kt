package io.getstream.chat.android.client.notifications

import com.google.firebase.messaging.RemoteMessage

interface FirebaseMessageParser {
    fun isValid(message: RemoteMessage): Boolean
    fun parse(message: RemoteMessage): Data

    data class Data(val messageId: String, val channelType: String, val channelId: String)
}