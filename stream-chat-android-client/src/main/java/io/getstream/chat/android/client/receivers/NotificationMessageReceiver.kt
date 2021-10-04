package io.getstream.chat.android.client.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message

internal class NotificationMessageReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_READ = "com.getstream.sdk.chat.READ"
        private const val ACTION_REPLY = "com.getstream.sdk.chat.REPLY"
        private const val ACTION_DISMISS = "com.getstream.sdk.chat.DISMISS"
        private const val KEY_NOTIFICATION_ID = "notification_id"
        private const val KEY_MESSAGE_ID = "message_id"
        private const val KEY_CHANNEL_ID = "id"
        private const val KEY_CHANNEL_TYPE = "type"
        private const val KEY_TEXT_REPLY = "text_reply"

        private val IMMUTABLE_PENDING_INTENT_FLAGS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        private val MUTABLE_PENDING_INTENT_FLAGS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        private fun createReplyPendingIntent(
            context: Context,
            notificationId: Int,
            channel: Channel,
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                notificationId,
                createNotifyIntent(context, notificationId, ACTION_REPLY).apply {
                    putExtra(KEY_CHANNEL_ID, channel.id)
                    putExtra(KEY_CHANNEL_TYPE, channel.type)
                },
                MUTABLE_PENDING_INTENT_FLAGS,
            )

        private fun createReadPendingIntent(
            context: Context,
            notificationId: Int,
            channel: Channel,
            message: Message,
        ): PendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            createNotifyIntent(context, notificationId, ACTION_READ).apply {
                putExtra(KEY_CHANNEL_ID, channel.id)
                putExtra(KEY_CHANNEL_TYPE, channel.type)
                putExtra(KEY_MESSAGE_ID, message.id)
            },
            IMMUTABLE_PENDING_INTENT_FLAGS,
        )

        internal fun createDismissPendingIntent(
            context: Context,
            notificationId: Int,
        ): PendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            createNotifyIntent(context, notificationId, ACTION_DISMISS),
            IMMUTABLE_PENDING_INTENT_FLAGS,
        )

        internal fun createReadAction(
            context: Context,
            notificationId: Int,
            channel: Channel,
            message: Message,
        ): NotificationCompat.Action {
            return NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_view,
                context.getString(R.string.stream_chat_notification_read),
                createReadPendingIntent(context, notificationId, channel, message),
            ).build()
        }

        internal fun createReplyAction(
            context: Context,
            notificationId: Int,
            channel: Channel,
        ): NotificationCompat.Action {
            val remoteInput =
                RemoteInput.Builder(KEY_TEXT_REPLY)
                    .setLabel(context.getString(R.string.stream_chat_notification_type_hint))
                    .build()
            return NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_send,
                context.getString(R.string.stream_chat_notification_reply),
                createReplyPendingIntent(context, notificationId, channel)
            )
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build()
        }

        private fun createNotifyIntent(context: Context, notificationId: Int, action: String) =
            Intent(context, NotificationMessageReceiver::class.java).apply {
                putExtra(KEY_NOTIFICATION_ID, notificationId)
                this.action = action
            }
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
        }
        intent?.getIntExtra(KEY_NOTIFICATION_ID, 0)?.let(::cancelNotification)
    }

    private fun markAsRead(messageId: String?, channelId: String?, channelType: String?) {
        if (!ChatClient.isInitialized) return

        if (messageId.isNullOrBlank() || channelId.isNullOrBlank() || channelType.isNullOrBlank()) {
            return
        }

        ChatClient.instance().markMessageRead(channelType, channelId, messageId).enqueue()
    }

    private fun replyText(
        channelId: String?,
        type: String?,
        messageChars: CharSequence?,
    ) {
        if (!ChatClient.isInitialized) return

        val currentUser = ChatClient.instance().getCurrentUser()

        if (messageChars.isNullOrBlank() || channelId.isNullOrBlank() || type.isNullOrBlank() || currentUser == null) {
            return
        }

        ChatClient.instance().sendMessage(
            channelType = type,
            channelId = channelId,
            message = Message().apply {
                text = messageChars.toString()
                user = currentUser
            }
        ).enqueue()
    }

    private fun cancelNotification(notificationId: Int) {
        ChatClient.dismissNotification(notificationId)
    }
}
