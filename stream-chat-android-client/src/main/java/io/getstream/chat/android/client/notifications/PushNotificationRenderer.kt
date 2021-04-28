package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.extensions.sendImplicitBroadcast
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver

public open class PushNotificationRenderer : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val channelName: String = intent.getStringExtra(EXTRA_CHANNEL_NAME)
        val messageText = intent.getStringExtra(EXTRA_MESSAGE_TEXT)
        val messageId = intent.getStringExtra(EXTRA_MESSAGE_ID)
        val channelType = intent.getStringExtra(EXTRA_CHANNEL_TYPE)
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)

        val notification = if (channelName.isNullOrEmpty() && messageText.isNullOrEmpty()) {
            buildErrorCaseNotification(context)
        } else {
            buildNotification(
                context,
                notificationId,
                channelName,
                messageText,
                messageId,
                channelType,
                channelId
            )
        }

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.notify(
            notificationId,
            notification
        )
    }

    public open fun buildNotification(
        context: Context,
        notificationId: Int,
        channelName: String,
        messageText: String,
        messageId: String,
        channelType: String,
        channelId: String,
    ): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = getNotificationBuilder(context)

        val intent = PendingIntent.getActivity(
            context,
            getRequestCode(),
            getNewMessageIntent(context, messageId, channelType, channelId),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationBuilder.setContentTitle(channelName)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShowWhen(true)
            .setContentIntent(intent)
            .setSound(defaultSoundUri)

        notificationBuilder.apply {
            addAction(
                getReadAction(
                    context,
                    preparePendingIntent(
                        context,
                        notificationId,
                        messageId,
                        channelId,
                        channelType,
                        NotificationMessageReceiver.ACTION_READ
                    )
                )
            )
            addAction(
                getReplyAction(
                    context,
                    preparePendingIntent(
                        context,
                        notificationId,
                        messageId,
                        channelId,
                        channelType,
                        NotificationMessageReceiver.ACTION_REPLY
                    )
                )
            )
        }

        return notificationBuilder.build()
    }

    public open fun getNewMessageIntent(
        context: Context,
        messageId: String,
        channelType: String,
        channelId: String,
    ): Intent {
        return context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
    }

    public open fun buildErrorCaseNotification(context: Context): Notification {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = getNotificationBuilder(context)
        val intent = PendingIntent.getActivity(
            context,
            getRequestCode(),
            getErrorCaseIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return notificationBuilder.setContentTitle(getErrorCaseNotificationTitle(context))
            .setContentText(getErrorCaseNotificationContent(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShowWhen(true)
            .setContentIntent(intent)
            .setSound(defaultSoundUri)
            .build()
    }

    public open fun getErrorCaseIntent(context: Context): Intent {
        return context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
    }

    protected open fun getSmallIcon(): Int {
        return R.drawable.stream_ic_notification
    }

    public open fun getErrorCaseNotificationTitle(context: Context): String {
        return context.getString(R.string.stream_chat_notification_title)
    }

    public open fun getErrorCaseNotificationContent(context: Context): String {
        return context.getString(R.string.stream_chat_notification_content)
    }

    public open fun getNotificationChannelId(context: Context): String {
        return context.getString(R.string.stream_chat_notification_channel_id)
    }

    private fun preparePendingIntent(
        context: Context,
        notificationId: Int,
        messageId: String,
        channelId: String,
        type: String,
        actionType: String,
    ): PendingIntent {
        val notifyIntent = Intent(context, NotificationMessageReceiver::class.java)

        notifyIntent.apply {
            putExtra(NotificationMessageReceiver.KEY_NOTIFICATION_ID, notificationId)
            putExtra(NotificationMessageReceiver.KEY_MESSAGE_ID, messageId)
            putExtra(NotificationMessageReceiver.KEY_CHANNEL_ID, channelId)
            putExtra(NotificationMessageReceiver.KEY_CHANNEL_TYPE, type)
            action = actionType
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getReadAction(context: Context, pendingIntent: PendingIntent): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_view,
            context.getString(R.string.stream_chat_notification_read),
            pendingIntent
        ).build()
    }

    private fun getReplyAction(context: Context, replyPendingIntent: PendingIntent): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(NotificationMessageReceiver.KEY_TEXT_REPLY)
            .setLabel(context.getString(R.string.stream_chat_notification_type_hint))
            .build()
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            context.getString(R.string.stream_chat_notification_reply),
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun getNotificationBuilder(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, getNotificationChannelId(context))
            .setAutoCancel(true)
            .setSmallIcon(getSmallIcon())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
    }

    private fun getRequestCode(): Int {
        return 1220999987
    }

    public companion object {
        private const val ACTION_SHOW_NOTIFICATION: String = "io.getstream.chat.SHOW_NOTIFICATION"

        private const val EXTRA_NOTIFICATION_ID: String = "notificationId"
        private const val EXTRA_CHANNEL_NAME: String = "channelName"
        private const val EXTRA_MESSAGE_TEXT: String = "messageText"
        private const val EXTRA_MESSAGE_ID: String = "messageId"
        private const val EXTRA_CHANNEL_TYPE: String = "channelType"
        private const val EXTRA_CHANNEL_ID: String = "channelId"

        public fun showMessageNotification(
            context: Context,
            channelName: String,
            messageText: String,
            messageId: String,
            channelType: String,
            channelId: String,
        ) {
            val notificationId = System.currentTimeMillis().toInt()
            context.sendImplicitBroadcast(
                Intent(ACTION_SHOW_NOTIFICATION).apply {
                    putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                    putExtra(EXTRA_CHANNEL_NAME, channelName)
                    putExtra(EXTRA_MESSAGE_TEXT, messageText)
                    putExtra(EXTRA_MESSAGE_ID, messageId)
                    putExtra(EXTRA_CHANNEL_TYPE, channelType)
                    putExtra(EXTRA_CHANNEL_ID, channelId)
                }
            )
        }
    }
}
