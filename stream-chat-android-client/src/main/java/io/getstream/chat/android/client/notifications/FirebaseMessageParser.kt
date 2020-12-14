package io.getstream.chat.android.client.notifications

import com.google.firebase.messaging.RemoteMessage

public interface FirebaseMessageParser {
    public fun isValidRemoteMessage(message: RemoteMessage): Boolean
    public fun parse(message: RemoteMessage): Data

    public data class Data(val messageId: String, val channelType: String, val channelId: String)
}
