package io.getstream.chat.android.client.notifications.handler

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.getstream.chat.android.client.R

public data class NotificationConfig(
    @StringRes val notificationChannelId: Int = R.string.stream_chat_notification_channel_id,
    @StringRes val notificationChannelName: Int = R.string.stream_chat_notification_channel_name,
    @DrawableRes val smallIcon: Int = R.drawable.stream_ic_notification,
    @StringRes val errorCaseNotificationTitle: Int = R.string.stream_chat_notification_title,
    @StringRes val errorCaseNotificationContent: Int = R.string.stream_chat_notification_content,
    @StringRes val loadNotificationDataChannelName: Int = R.string.stream_chat_load_notification_data_title,
    @DrawableRes val loadNotificationDataIcon: Int = R.drawable.stream_ic_notification,
    @StringRes val loadNotificationDataTitle: Int = R.string.stream_chat_load_notification_data_title,
    @StringRes val notificationGroupSummaryContentText: Int = R.string.stream_chat_notification_group_summary_content_text,
    @StringRes val errorNotificationGroupSummaryTitle: Int = R.string.stream_chat_error_notification_group_summary_content_text,
    @StringRes val errorNotificationGroupSummaryContentText: Int = R.string.stream_chat_error_notification_group_summary_content_text,
    @Deprecated(
        message = "Notifications are grouped by channel by default. This behavior can be changed by handling the notification",
        level = DeprecationLevel.WARNING,
    ) val shouldGroupNotifications: Boolean = false,
    val pushNotificationsEnabled: Boolean = true,
    val pushDeviceGenerators: List<PushDeviceGenerator> = listOf(),
)
