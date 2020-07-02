package io.getstream.chat.sample.application

import android.content.Context
import io.getstream.chat.android.livedata.ChatNotificationConfigOffline

class NotificationsConfig(context: Context) : ChatNotificationConfigOffline(context) {
    override fun getFirebaseMessageIdKey(): String = "message_id"
    override fun getFirebaseChannelIdKey(): String = "channel_id"
    override fun getFirebaseChannelTypeKey(): String = "channel_type"
}