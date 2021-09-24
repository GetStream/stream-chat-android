@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client.notifications.handler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.PushMessage
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.NotificationLoadDataListener
import io.getstream.chat.android.client.receivers.NotificationMessageReceiver

/**
 * Class responsible for handling chat notifications.
 */
public open class ChatNotificationHandler @JvmOverloads constructor(
    protected val context: Context,
    public val config: NotificationConfig = NotificationConfig(),
) {

    private val sharedPreferences: SharedPreferences by lazy { context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE) }
    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    /**
     * Handles showing notification after receiving [NewMessageEvent] from other users.
     * Default implementation loads necessary data and displays notification even if app is in foreground.
     *
     * @return False if notification should be handled internally.
     */
    public open fun onChatEvent(event: NewMessageEvent): Boolean {
        return true
    }

    /**
     * Handles showing notification after receiving [PushMessage].
     * Default implementation loads necessary data from the server and shows notification if application is not in foreground.
     *
     * @return False if remote message should be handled internally.
     */
    public open fun onPushMessage(message: PushMessage): Boolean {
        return false
    }

    public open fun getDeviceRegisteredListener(): DeviceRegisteredListener? {
        return null
    }

    @Deprecated(
        message = "It is not used anymore, you will be notify to build the notification instead",
        level = DeprecationLevel.ERROR,
    )
    public open fun getDataLoadListener(): NotificationLoadDataListener? {
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public open fun createNotificationChannel(): NotificationChannel {
        return NotificationChannel(
            getNotificationChannelId(),
            getNotificationChannelName(),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            setShowBadge(true)
            importance = NotificationManager.IMPORTANCE_HIGH
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(
                100,
                200,
                300,
                400,
                500,
                400,
                300,
                200,
                400,
            )
        }
    }

    public open fun getNotificationChannelId(): String = context.getString(config.notificationChannelId)

    public open fun getNotificationChannelName(): String =
        context.getString(config.notificationChannelName)

    public open fun getErrorCaseNotificationTitle(): String =
        context.getString(config.errorCaseNotificationTitle)

    public open fun getErrorCaseNotificationContent(): String =
        context.getString(config.errorCaseNotificationContent)

    public open fun buildErrorCaseNotification(): Notification {
        return getNotificationBuilder(
            contentTitle = getErrorCaseNotificationTitle(),
            contentText = getErrorCaseNotificationContent(),
            groupKey = getErrorNotificationGroupKey(),
            intent = getErrorCaseIntent(),
        ).build()
    }

    internal fun showNotification(channel: Channel, message: Message) {
        val notificationId: Int = System.nanoTime().toInt()
        val notificationSummaryId = getNotificationGroupSummaryId(channel.type, channel.id)
        addNotificationId(notificationId, notificationSummaryId)
        showNotification(notificationId, buildNotification(notificationId, channel, message).build())
        showNotification(notificationSummaryId, buildNotificationGroupSummary(channel, message).build())
    }

    public open fun buildNotification(
        notificationId: Int,
        channel: Channel,
        message: Message,
    ): NotificationCompat.Builder {
        return getNotificationBuilder(
            contentTitle = channel.getNotificationContentTitle(),
            contentText = message.text,
            groupKey = getNotificationGroupKey(channelType = channel.type, channelId = channel.id),
            intent = getNewMessageIntent(messageId = message.id, channelType = channel.type, channelId = channel.id),
        ).apply {
            addAction(NotificationMessageReceiver.createReadAction(context, notificationId, channel, message))
            addAction(NotificationMessageReceiver.createReplyAction(context, notificationId, channel))
            setDeleteIntent(NotificationMessageReceiver.createDismissPendingIntent(context, notificationId))
        }
    }

    public open fun buildNotificationGroupSummary(channel: Channel, message: Message): NotificationCompat.Builder {
        return getNotificationBuilder(
            contentTitle = channel.getNotificationContentTitle(),
            contentText = context.getString(config.notificationGroupSummaryContentText),
            groupKey = getNotificationGroupKey(channelType = channel.type, channelId = channel.id),
            intent = getNewMessageIntent(messageId = message.id, channelType = channel.type, channelId = channel.id),
        ).apply {
            setGroupSummary(true)
        }
    }

    public open fun buildErrorNotificationGroupSummary(): Notification {
        return getNotificationBuilder(
            contentTitle = context.getString(config.errorNotificationGroupSummaryTitle),
            contentText = context.getString(config.errorNotificationGroupSummaryContentText),
            groupKey = getErrorNotificationGroupKey(),
            getErrorCaseIntent(),
        ).apply {
            setGroupSummary(true)
        }.build()
    }

    public open fun getNotificationGroupKey(channelType: String, channelId: String): String {
        return "$channelType:$channelId"
    }

    public open fun getNotificationGroupSummaryId(channelType: String, channelId: String): Int {
        return getNotificationGroupKey(channelType = channelType, channelId = channelId).hashCode()
    }

    public open fun getErrorNotificationGroupKey(): String = ERROR_NOTIFICATION_GROUP_KEY

    public open fun getErrorNotificationGroupSummaryId(): Int = getErrorNotificationGroupKey().hashCode()

    private fun getRequestCode(): Int {
        return System.currentTimeMillis().toInt()
    }

    public open fun getNewMessageIntent(
        messageId: String,
        channelType: String,
        channelId: String,
    ): Intent {
        return context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
    }

    /**
     * Dismiss notifications from a given [channelType] and [channelId].
     *
     * @param channelType String that represent the channel type of the channel you want to dismiss notifications.
     * @param channelId String that represent the channel id of the channel you want to dismiss notifications.
     */
    internal fun dismissChannelNotifications(channelType: String, channelId: String) {
        dismissSummaryNotification(getNotificationGroupSummaryId(channelType, channelId))
    }

    /**
     * Dismiss all notifications.
     */
    internal fun dismissAllNotifications() {
        getNotificationSummaryIds().forEach(::dismissSummaryNotification)
    }

    public open fun getErrorCaseIntent(): Intent {
        return context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!
    }

    private fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    private fun getNotificationBuilder(
        contentTitle: String,
        contentText: String,
        groupKey: String,
        intent: Intent,
    ): NotificationCompat.Builder {
        val contentIntent = PendingIntent.getActivity(
            context,
            getRequestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(context, getNotificationChannelId())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setSmallIcon(config.smallIcon)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShowWhen(true)
            .setContentIntent(contentIntent)
            .setGroup(groupKey)
    }

    internal fun onCreateDevice(onDeviceCreated: (device: Device) -> Unit) {
        config.pushDeviceGenerators.firstOrNull { it.isValidForThisDevice(context) }
            ?.asyncGenerateDevice(onDeviceCreated)
    }

    private fun Channel.getNotificationContentTitle(): String =
        name.takeIf { it.isNotEmpty() }
            ?: getMemberNamesWithoutCurrentUser()
            ?: context.getString(R.string.stream_chat_notification_title)

    private fun Channel.getMemberNamesWithoutCurrentUser(): String? = getUsersExcludingCurrent()
        .joinToString { it.name }
        .takeIf { it.isNotEmpty() }

    private fun dismissSummaryNotification(notificationSummaryId: Int) {
        getAssociatedNotificationIds(notificationSummaryId).forEach {
            notificationManager.cancel(it)
            removeNotificationId(it)
        }
        notificationManager.cancel(notificationSummaryId)
        sharedPreferences.edit { remove(getNotificationSummaryIdKey(notificationSummaryId)) }
    }

    internal fun onDismissNotification(notificationId: Int) {
        val notificationSummaryId = getAssociatedNotificationSummaryId(notificationId)
        removeNotificationId(notificationId)
        notificationManager.cancel(notificationId)
        if (getAssociatedNotificationIds(notificationSummaryId).isNullOrEmpty()) {
            notificationManager.cancel(notificationSummaryId)
        }
    }

    private fun addNotificationId(notificationId: Int, notificationSummaryId: Int) {
        sharedPreferences.edit {
            putInt(getNotificationIdKey(notificationId), notificationSummaryId)
            putStringSet(
                KEY_NOTIFICATION_SUMMARY_IDS,
                (getNotificationSummaryIds() + notificationSummaryId).map(Int::toString).toSet()
            )
            putStringSet(
                getNotificationSummaryIdKey(notificationSummaryId),
                (getAssociatedNotificationIds(notificationSummaryId) + notificationId).map(Int::toString).toSet()
            )
        }
    }

    private fun removeNotificationId(notificationId: Int) {
        sharedPreferences.edit {
            val notificationSummaryId = getAssociatedNotificationSummaryId(notificationId)
            remove(getNotificationIdKey(notificationId))
            putStringSet(
                getNotificationSummaryIdKey(notificationSummaryId),
                (getAssociatedNotificationIds(notificationSummaryId) - notificationId).map(Int::toString).toSet()
            )
        }
    }

    private fun getNotificationSummaryIds(): Set<Int> = sharedPreferences.getStringSet(KEY_NOTIFICATION_SUMMARY_IDS, null).orEmpty().map(String::toInt).toSet()
    private fun getAssociatedNotificationSummaryId(notificationId: Int): Int = sharedPreferences.getInt(getNotificationIdKey(notificationId), 0)
    private fun getAssociatedNotificationIds(notificationSummaryId: Int): Set<Int> =
        sharedPreferences.getStringSet(getNotificationSummaryIdKey(notificationSummaryId), null).orEmpty().map(String::toInt).toSet()

    private fun getNotificationIdKey(notificationId: Int) = KEY_PREFIX_NOTIFICATION_ID + notificationId
    private fun getNotificationSummaryIdKey(notificationSummaryId: Int) = KEY_PREFIX_NOTIFICATION_SUMMARY_ID + notificationSummaryId

    private companion object {
        private const val ERROR_NOTIFICATION_GROUP_KEY = "error_notification_group_key"
        private const val SHARED_PREFERENCES_NAME = "stream_notifications.sp"
        private const val KEY_PREFIX_NOTIFICATION_ID = "nId-"
        private const val KEY_PREFIX_NOTIFICATION_SUMMARY_ID = "nSId-"
        private const val KEY_NOTIFICATION_SUMMARY_IDS = "notification_summary_ids"
    }
}
