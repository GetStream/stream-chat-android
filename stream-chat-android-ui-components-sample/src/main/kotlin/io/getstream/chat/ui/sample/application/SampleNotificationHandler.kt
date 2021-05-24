package io.getstream.chat.ui.sample.application

import android.content.Context
import android.content.Intent
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.ui.sample.feature.HostActivity

const val EXTRA_CHANNEL_ID = "extra_channel_id"
const val EXTRA_CHANNEL_TYPE = "extra_channel_type"
const val EXTRA_MESSAGE_ID = "extra_message_id"

class SampleNotificationHandler(context: Context, notificationConfig: NotificationConfig) :
    ChatNotificationHandler(context, notificationConfig) {

    override fun getNewMessageIntent(
        messageId: String,
        channelType: String,
        channelId: String
    ): Intent = Intent(context, HostActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_CHANNEL_ID, channelId)
        putExtra(EXTRA_CHANNEL_TYPE, channelType)
        putExtra(EXTRA_MESSAGE_ID, messageId)
    }
}
