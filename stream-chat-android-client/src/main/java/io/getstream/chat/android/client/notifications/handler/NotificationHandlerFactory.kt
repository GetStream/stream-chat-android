/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.notifications.handler

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import io.getstream.android.push.permissions.DefaultNotificationPermissionHandler
import io.getstream.android.push.permissions.NotificationPermissionHandler
import io.getstream.chat.android.client.R
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage
import io.getstream.chat.android.models.User
import kotlin.reflect.full.primaryConstructor

/**
 * Factory for default [NotificationHandler].
 * Use it to customize an intent the user triggers when clicking on a notification.
 */
public object NotificationHandlerFactory {

    /**
     * Method that creates a [NotificationHandler].
     *
     * @param context The [Context] to build the [NotificationHandler] with.
     * @param notificationConfig Configuration for push notifications.
     * @param newMessageIntent Lambda expression used to generate an [Intent] to open your app
     * @param notificationChannel Lambda expression used to generate a [NotificationChannel].
     * Used in SDK_INT >= VERSION_CODES.O.
     * @param userIconBuilder Generates [IconCompat] to be shown on notifications.
     * @param permissionHandler Handles [android.Manifest.permission.POST_NOTIFICATIONS] permission lifecycle.
     * @param notificationTextFormatter Lambda expression used to formats the text of the notification.
     * @param actionsProvider Lambda expression used to provide actions for the notification.
     * See [NotificationActionsFactory] for customizing the default actions.
     * @param notificationBuilderTransformer Lambda expression used to transform the [NotificationCompat.Builder]
     * before building the notification.
     * @param onPushMessage Lambda expression called when a new push message is received. Return true if the
     * push message was handled and no further processing is required.
     *
     * @return A [NotificationHandler] instance.
     * @see NotificationHandler
     */
    @SuppressLint("NewApi")
    @JvmOverloads
    @JvmStatic
    public fun createNotificationHandler(
        context: Context,
        notificationConfig: NotificationConfig,
        newMessageIntent: ((message: Message, channel: Channel) -> Intent) = getDefaultNewMessageIntentFun(context),
        notificationChannel: (() -> NotificationChannel) = getDefaultNotificationChannel(context),
        userIconBuilder: UserIconBuilder = provideDefaultUserIconBuilder(context),
        permissionHandler: NotificationPermissionHandler? = provideDefaultNotificationPermissionHandler(context),
        notificationTextFormatter: ((currentUser: User?, message: Message) -> CharSequence) =
            getDefaultNotificationTextFormatter(notificationConfig),
        actionsProvider: (notificationId: Int, channel: Channel, message: Message) -> List<NotificationCompat.Action> =
            getDefaultActionsProvider(context),
        notificationBuilderTransformer: (NotificationCompat.Builder, ChatNotification) -> NotificationCompat.Builder =
            { builder, _ -> builder },
        onPushMessage: (pushMessage: PushMessage) -> Boolean = { false },
    ): NotificationHandler = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        MessagingStyleNotificationHandler(
            context = context,
            newMessageIntent = newMessageIntent,
            notificationChannel = notificationChannel,
            userIconBuilder = userIconBuilder,
            permissionHandler = permissionHandler,
            notificationTextFormatter = notificationTextFormatter,
            actionsProvider = actionsProvider,
            notificationBuilderTransformer = notificationBuilderTransformer,
            onNewPushMessage = onPushMessage,
        )
    } else {
        ChatNotificationHandler(
            context = context,
            newMessageIntent = newMessageIntent,
            notificationChannel = notificationChannel,
            notificationTextFormatter = notificationTextFormatter,
            actionsProvider = actionsProvider,
            notificationBuilderTransformer = notificationBuilderTransformer,
            onNewPushMessage = onPushMessage,
        )
    }

    private fun getDefaultActionsProvider(
        context: Context,
    ): (notificationId: Int, channel: Channel, message: Message) -> List<NotificationCompat.Action> =
        { notificationId, channel, message ->
            listOf(
                NotificationActionsFactory.createMarkReadAction(context, notificationId, channel, message),
                NotificationActionsFactory.createReplyAction(context, notificationId, channel),
            )
        }

    private fun getDefaultNotificationTextFormatter(
        notificationConfig: NotificationConfig,
    ): (currentUser: User?, message: Message) -> CharSequence = { currentUser, message ->
        when (notificationConfig.autoTranslationEnabled) {
            true -> currentUser?.language?.let { userLanguage ->
                message.getTranslation(userLanguage).ifEmpty { message.text }
            } ?: message.text
            else -> message.text
        }
    }

    private fun getDefaultNewMessageIntentFun(
        context: Context,
    ): (message: Message, channel: Channel) -> Intent {
        return { _, _ -> createDefaultNewMessageIntent(context) }
    }

    private fun createDefaultNewMessageIntent(context: Context): Intent =
        context.packageManager!!.getLaunchIntentForPackage(context.packageName)!!

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDefaultNotificationChannel(context: Context): (() -> NotificationChannel) {
        return {
            NotificationChannel(
                context.getString(R.string.stream_chat_notification_channel_id),
                context.getString(R.string.stream_chat_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        }
    }

    private fun provideDefaultUserIconBuilder(context: Context): UserIconBuilder {
        // We search for the StreamCoilUserIconBuilder by reflection and this is slow - we need
        // to postpone to not block the SDK initialisation
        return object : UserIconBuilder {
            private val builder by lazy {
                val appContext = context.applicationContext
                runCatching {
                    Class.forName(
                        "io.getstream.chat.android.ui.common.notifications." +
                            "StreamCoilUserIconBuilder",
                    )
                        .kotlin.primaryConstructor
                        ?.call(appContext) as UserIconBuilder
                }.getOrDefault(DefaultUserIconBuilder(appContext))
            }

            override suspend fun buildIcon(user: User): IconCompat? {
                return builder.buildIcon(user)
            }
        }
    }

    private fun provideDefaultNotificationPermissionHandler(context: Context): NotificationPermissionHandler {
        // We search for the SnackbarNotificationPermissionHandler by reflection and this is slow - we need
        // to postpone to not block the SDK initialisation
        return object : NotificationPermissionHandler {
            private val handler by lazy {
                val appContext = context.applicationContext
                runCatching {
                    Class.forName(
                        "io.getstream.android.push.permissions.snackbar.SnackbarNotificationPermissionHandler",
                    ).kotlin.primaryConstructor?.call(appContext) as NotificationPermissionHandler
                }.getOrDefault(
                    DefaultNotificationPermissionHandler
                        .createDefaultNotificationPermissionHandler(appContext as Application),
                )
            }

            override fun onPermissionDenied() {
                handler.onPermissionDenied()
            }

            override fun onPermissionGranted() {
                handler.onPermissionGranted()
            }

            override fun onPermissionRationale() {
                handler.onPermissionRationale()
            }

            override fun onPermissionRequested() {
                handler.onPermissionRequested()
            }
        }
    }
}
