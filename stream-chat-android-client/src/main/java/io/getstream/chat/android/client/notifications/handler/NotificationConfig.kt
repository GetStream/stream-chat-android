package io.getstream.chat.android.client.notifications.handler

import io.getstream.chat.android.client.R

public data class NotificationConfig(
    val notificationChannelId: Int = R.string.stream_chat_notification_channel_id,
    val notificationChannelName: Int = R.string.stream_chat_notification_channel_name,
    val smallIcon: Int = R.drawable.stream_ic_notification,
    val firebaseMessageIdKey: String = "message_id",
    val firebaseMessageTextKey: String = "message_text",
    val firebaseChannelIdKey: String = "channel_id",
    val firebaseChannelTypeKey: String = "channel_type",
    val firebaseChannelNameKey: String = "channel_name",
    val errorCaseNotificationTitle: Int = R.string.stream_chat_notification_title,
    val errorCaseNotificationContent: Int = R.string.stream_chat_notification_content,
    val useProvidedFirebaseInstance: Boolean = true,
    val loadNotificationDataChannelName: Int = R.string.stream_chat_load_notification_data_title,
    val loadNotificationDataIcon: Int = R.drawable.stream_ic_notification,
    val loadNotificationDataTitle: Int = R.string.stream_chat_load_notification_data_title,
    val notificationGroupSummaryContentText: Int = R.string.stream_chat_notification_group_summary_content_text,
    val errorNotificationGroupSummaryTitle: Int = R.string.stream_chat_error_notification_group_summary_content_text,
    val errorNotificationGroupSummaryContentText: Int = R.string.stream_chat_error_notification_group_summary_content_text,
    val shouldGroupNotifications: Boolean = false,
    val pushNotificationsEnabled: Boolean = true,
)
