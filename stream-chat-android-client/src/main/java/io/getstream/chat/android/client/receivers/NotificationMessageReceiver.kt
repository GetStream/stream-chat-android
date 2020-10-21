package io.getstream.chat.android.client.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.safeLet
import io.getstream.chat.android.client.models.Message

internal class NotificationMessageReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_READ = "com.getstream.sdk.chat.READ"
        const val ACTION_REPLY = "com.getstream.sdk.chat.REPLY"
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_MESSAGE_ID = "message_id"
        const val KEY_CHANNEL_ID = "id"
        const val KEY_CHANNEL_TYPE = "type"
        const val KEY_TEXT_REPLY = "text_reply"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_READ -> markAsRead(
                intent.getStringExtra(KEY_MESSAGE_ID),
                intent.getStringExtra(KEY_CHANNEL_ID),
                intent.getStringExtra(KEY_CHANNEL_TYPE)
            )
            ACTION_REPLY -> {
                val results = RemoteInput.getResultsFromIntent(intent)
                if (results != null) {
                    replyText(
                        intent.getStringExtra(KEY_CHANNEL_ID),
                        intent.getStringExtra(KEY_CHANNEL_TYPE),
                        results.getCharSequence(KEY_TEXT_REPLY)
                    )
                }
            }
            else -> {
                // Unsupported action
            }
        }
        cancelNotification(context, intent?.getIntExtra(KEY_NOTIFICATION_ID, 0))
    }

    private fun markAsRead(messageId: String?, channelId: String?, channelType: String?) {
        if (messageId.isNullOrBlank() || channelId.isNullOrBlank() || channelType.isNullOrBlank()) {
            // Log.e(TAG, "Invalid replyText  parameters: id:$id type:$type")
            return
        }

        ChatClient.instance().markMessageRead(channelType, channelId, messageId).enqueue { result ->
            if (result.isSuccess) {
                // Log.i(TAG, "Channel marked as read")
            } else {
                // Log.e(TAG, "Cant mark as read. Error: $errMsg Code: $errCode")
            }
        }
    }

    private fun replyText(
        channelId: String?,
        type: String?,
        messageChars: CharSequence?
    ) {
        if (channelId.isNullOrBlank() || channelId.isNullOrBlank() || type.isNullOrBlank()) {
            // Log.e(TAG, "Invalid replyText  parameters: id:$id type:$type")
            return
        }

        ChatClient.instance().sendMessage(
            channelType = type,
            channelId = channelId,
            message = Message().apply {
                text = messageChars.toString()
            }
        ).enqueue { result ->
            if (result.isSuccess) {
                // Log.i(TAG, "Channel marked as read")
            } else {
                // Log.e(TAG, "Cant mark as read. Error: $errMsg Code: $errCode")
            }
        }
    }

    private fun cancelNotification(
        context: Context?,
        notificationId: Int?
    ) {
        safeLet(
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager,
            notificationId
        ) { notificationManager, id ->
            notificationManager.cancel(id)
        }
    }
}
