package io.getstream.chat.android.client.notifications.handler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.edit
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver
import java.util.Date

@RequiresApi(Build.VERSION_CODES.M)
internal class MessagingStyleNotificationHandler(
    private val context: Context,
    private val newMessageIntent: (messageId: String, channelType: String, channelId: String) -> Intent =
        { _, _, _ -> context.packageManager!!.getLaunchIntentForPackage(context.packageName)!! }
) : NotificationHandler {
    private val sharedPreferences: SharedPreferences by lazy { context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE) }
    private val notificationManager: NotificationManager by lazy {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.createNotificationChannel(createNotificationChannel())
            }
        }
    }

    override fun showNotification(channel: Channel, message: Message) {
        val currentUser = ChatClient.instance().getCurrentUser() ?: return
        val notificationId = createNotificationId(channel.type, channel.id)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            newMessageIntent(message.id, channel.type, channel.id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val initialMessagingStyle = restoreMessagingStyle(channel) ?: createMessagingStyle(currentUser, channel)
        val notification = NotificationCompat.Builder(context, getNotificationChannelId())
            .setSmallIcon(R.drawable.stream_ic_notification)
            .setStyle(initialMessagingStyle.addMessage(message.toMessagingStyleMessage()))
            .setContentIntent(contentPendingIntent)
            .addAction(NotificationMessageReceiver.createReadAction(context, notificationId, channel, message))
            .addAction(NotificationMessageReceiver.createReplyAction(context, notificationId, channel))
            .build()
        addNotificationId(notificationId)
        notificationManager.notify(notificationId, notification)
    }

    override fun dismissChannelNotifications(channelType: String, channelId: String) {
        dismissNotification(createNotificationId(channelType, channelId))
    }

    override fun dismissAllNotifications() {
        getShownNotifications().forEach(::dismissNotification)
    }

    private fun dismissNotification(notificationId: Int) {
        removeNotificationId(notificationId)
        notificationManager.cancel(notificationId)
    }

    private fun addNotificationId(notificationId: Int) {
        sharedPreferences.edit {
            putStringSet(KEY_NOTIFICATIONS_SHOWN, (getShownNotifications() + notificationId).map(Int::toString).toSet())
        }
    }

    private fun removeNotificationId(notificationId: Int) {
        sharedPreferences.edit {
            putStringSet(KEY_NOTIFICATIONS_SHOWN, (getShownNotifications() - notificationId).map(Int::toString).toSet())
        }
    }

    private fun getShownNotifications(): Set<Int> =
        sharedPreferences.getStringSet(KEY_NOTIFICATIONS_SHOWN, null).orEmpty().map(String::toInt).toSet()

    private fun createNotificationId(channelType: String, channelId: String): Int = "$channelType:$channelId".hashCode()

    private fun restoreMessagingStyle(channel: Channel): NotificationCompat.MessagingStyle? =
        notificationManager.activeNotifications
            .firstOrNull { it.id == createNotificationId(channel.type, channel.id) }
            ?.notification
            ?.let(NotificationCompat.MessagingStyle::extractMessagingStyleFromNotification)

    private fun createMessagingStyle(currentUser: User, channel: Channel): NotificationCompat.MessagingStyle =
        NotificationCompat.MessagingStyle(currentUser.toPerson())
            .setConversationTitle(channel.name)
            .setGroupConversation(channel.name.isNotBlank())

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(): NotificationChannel {
        return NotificationChannel(
            getNotificationChannelId(),
            context.getString(R.string.stream_chat_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
    }

    private fun getNotificationChannelId() = context.getString(R.string.stream_chat_notification_channel_id)
    private companion object {
        private const val SHARED_PREFERENCES_NAME = "stream_notifications.sp"
        private const val KEY_NOTIFICATIONS_SHOWN = "KEY_NOTIFICATIONS_SHOWN"
    }
}

private fun Message.toMessagingStyleMessage(): NotificationCompat.MessagingStyle.Message =
    NotificationCompat.MessagingStyle.Message(text, timestamp, person)

private val Message.person: Person
    get() = user.toPerson()

private val Message.timestamp: Long
    get() = (createdAt ?: createdLocallyAt ?: Date()).time

private fun User.toPerson(): Person =
    Person.Builder()
        .setKey(id)
        .setName(name)
        .build()
