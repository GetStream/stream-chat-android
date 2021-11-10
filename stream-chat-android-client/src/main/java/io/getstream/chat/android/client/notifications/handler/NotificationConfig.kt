package io.getstream.chat.android.client.notifications.handler

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.getstream.chat.android.client.R

/**
 * Push notifications configuration class
 */
public data class NotificationConfig(
    @Deprecated(
        message = "You need to override this value in your string.xml resources using the key `stream_chat_notification_channel_id`",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val notificationChannelId: Int = R.string.stream_chat_notification_channel_id,

    @Deprecated(
        message = "You need to override this value in your string.xml resources using the key `stream_chat_notification_channel_name`",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val notificationChannelName: Int = R.string.stream_chat_notification_channel_name,

    @Deprecated(
        message = "You need to override this drawable in your resources using the drawable name `stream_ic_notification`",
        level = DeprecationLevel.ERROR,
    )
    @DrawableRes val smallIcon: Int = R.drawable.stream_ic_notification,

    @Deprecated(
        message = "It is not used anymore",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val errorCaseNotificationTitle: Int = R.string.stream_chat_notification_title,

    @Deprecated(
        message = "It is not used anymore",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val errorCaseNotificationContent: Int = R.string.stream_chat_notification_content,

    @Deprecated(
        message = "It is not used anymore",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val loadNotificationDataChannelName: Int = R.string.stream_chat_load_notification_data_title,

    @Deprecated(
        message = "You need to override this drawable in your resources using the drawable name `stream_ic_notification`",
        level = DeprecationLevel.ERROR,
    )
    @DrawableRes val loadNotificationDataIcon: Int = R.drawable.stream_ic_notification,

    @Deprecated(
        message = "You need to override this value in your string.xml resources using the key `stream_chat_load_notification_data_title`",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val loadNotificationDataTitle: Int = R.string.stream_chat_load_notification_data_title,

    @Deprecated(
        message = "You need to override this value in your string.xml resources using the key `stream_chat_notification_group_summary_content_text`",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val notificationGroupSummaryContentText: Int = R.string.stream_chat_notification_group_summary_content_text,

    @Deprecated(
        message = "It is not used anymore",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val errorNotificationGroupSummaryTitle: Int = R.string.stream_chat_error_notification_group_summary_content_text,

    @Deprecated(
        message = "It is not used anymore",
        level = DeprecationLevel.ERROR,
    )
    @StringRes val errorNotificationGroupSummaryContentText: Int = R.string.stream_chat_error_notification_group_summary_content_text,

    /**
     * Enables/disables push notifications on the device.
     * Device's token won't be registered if push notifications are disabled.
     */
    val pushNotificationsEnabled: Boolean = true,

    /**
     * A list of generators responsible for providing the information needed to register a device
     * @see [PushDeviceGenerator]
     */
    val pushDeviceGenerators: List<PushDeviceGenerator> = listOf(),

    /**
     * Allows enabling/disabling showing notification after receiving a push message.
     */
    val shouldShowNotificationOnPush: () -> Boolean = { true },
)
