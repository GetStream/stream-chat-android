package io.getstream.chat.ui.sample.application

import android.content.Context
import android.content.Intent
import io.getstream.chat.android.client.notifications.PushNotificationRenderer
import io.getstream.chat.ui.sample.feature.HostActivity

class SamplePushNotificationRenderer : PushNotificationRenderer() {

    override fun getNewMessageIntent(
        context: Context,
        messageId: String,
        channelType: String,
        channelId: String,
    ): Intent = Intent(context, HostActivity::class.java).apply {
        putExtra(EXTRA_CHANNEL_ID, channelId)
        putExtra(EXTRA_CHANNEL_TYPE, channelType)
        putExtra(EXTRA_MESSAGE_ID, messageId)
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
        const val EXTRA_CHANNEL_TYPE = "extra_channel_type"
        const val EXTRA_MESSAGE_ID = "extra_message_id"
    }
}
