package io.getstream.chat.android.client.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.safeLet

class NotificationMessageReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_READ = "com.getstream.sdk.chat.READ"
        const val ACTION_REPLY = "com.getstream.sdk.chat.REPLY"
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_CHANNEL_ID = "id"
        const val KEY_CHANNEL_TYPE = "type"
        const val KEY_TEXT_REPLY = "text_reply"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_READ -> markAsRead(
                context,
                intent.getStringExtra(KEY_CHANNEL_ID),
                intent.getStringExtra(KEY_CHANNEL_TYPE)
            )
            ACTION_REPLY -> {
                val results =
                    RemoteInput.getResultsFromIntent(intent)
                if (results != null) {
                    replyText(
                        context,
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
        cancelNotification(
            context,
            intent?.getIntExtra(KEY_NOTIFICATION_ID, 0)
        )
    }

    private fun markAsRead(
        context: Context?,
        id: String?,
        type: String?
    ) {
        /*if (id.isNullOrBlank() || type.isNullOrBlank()) {
            Log.e(TAG, "Invalid replyText  parameters: id:$id type:$type")
            return
        }
        val channel = Channel(StreamChat.getInstance(context), type, id)
        val config = Config()
        config.isReadEvents = true
        channel.config = config
        channel.markRead(object : InputMethodSession.EventCallback() {
            fun onSuccess(response: EventResponse?) {
                Log.i(TAG, "Channel marked as read")
            }

            fun onError(errMsg: String, errCode: Int) {
                Log.e(
                    NotificationMessageReceiver.TAG,
                    "Cant mark as read. Error: $errMsg Code: $errCode"
                )
            }
        })*/
    }

    private fun replyText(
        context: Context?,
        id: String?,
        type: String?,
        messageChars: CharSequence?
    ) {
        /*if (id == null || type == null || id.isEmpty() || type.isEmpty()) {
            Log.e(
                NotificationMessageReceiver.TAG,
                "Invalid replyText  parameters: id: $id type: $type"
            )
            return
        }
        if (messageChars == null || messageChars.toString().isEmpty()) {
            Log.e(
                NotificationMessageReceiver.TAG,
                "replyText: messageChars is empty or null: $messageChars"
            )
            return
        }
        val channel = Channel(StreamChat.getInstance(context), type, id)
        val config = Config()
        config.setReadEvents(true)
        channel.setConfig(config)
        val msg = Message()
        msg.setText(messageChars.toString())
        channel.sendMessage(msg, object : MessageCallback() {
            fun onSuccess(response: MessageResponse?) {
                Log.i(NotificationMessageReceiver.TAG, "Reply message sent success.")
            }

            fun onError(errMsg: String, errCode: Int) {
                Log.e(
                    NotificationMessageReceiver.TAG,
                    "Cant send reply. Error: $errMsg Code: $errCode"
                )
            }
        })*/
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