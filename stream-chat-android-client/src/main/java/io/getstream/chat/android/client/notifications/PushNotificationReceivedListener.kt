package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun interface PushNotificationReceivedListener {
    public fun onPushNotificationReceived(channelType: String, channelId: String)
}
