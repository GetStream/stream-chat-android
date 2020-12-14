package io.getstream.chat.android.client.extensions

import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.notifications.handler.NotificationConfig

public fun RemoteMessage.isValid(notificationConfig: NotificationConfig): Boolean =
    setOf(
        notificationConfig.firebaseMessageIdKey,
        notificationConfig.firebaseChannelIdKey,
        notificationConfig.firebaseChannelTypeKey
    )
        .fold(true) { valid, key -> valid && !data[key].isNullOrBlank() }
