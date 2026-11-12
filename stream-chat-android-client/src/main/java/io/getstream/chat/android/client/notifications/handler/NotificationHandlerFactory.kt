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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import io.getstream.android.push.permissions.NotificationPermissionHandler
import io.getstream.chat.android.client.R
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger

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
     * @param permissionHandler Handles [android.Manifest.permission.POST_NOTIFICATIONS] permission lifecycle, for
     * example to tell the user why notifications won't show after a denial. Nothing is shown by default.
     * @param notificationTextFormatter Lambda expression used to formats the text of the notification.
     * @param actionsProvider Lambda expression used to provide actions for the notification.
     * See [NotificationActionsFactory] for customizing the default actions.
     * @param notificationBuilderTransformer Lambda expression used to transform the [NotificationCompat.Builder]
     * before building the notification.
     * @param onPushMessage Lambda expression called when a new push message is received. Return true if the
     * push message was handled and no further processing is required.
     * @param notificationIdFactory Custom factory for generating notification IDs based on [ChatNotification].
     * If null or if [NotificationIdFactory.getNotificationId] returns null, the SDK will use its default
     * notification ID generation logic. Use this to customize how notifications are grouped.
     *
     * @return A [NotificationHandler] instance.
     * @see NotificationHandler
     * @see NotificationIdFactory
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
        permissionHandler: NotificationPermissionHandler? = null,
        notificationTextFormatter: ((currentUser: User?, message: Message) -> CharSequence) =
            getDefaultNotificationTextFormatter(notificationConfig),
        actionsProvider: (notificationId: Int, channel: Channel, message: Message) -> List<NotificationCompat.Action> =
            getDefaultActionsProvider(context),
        notificationBuilderTransformer: (NotificationCompat.Builder, ChatNotification) -> NotificationCompat.Builder =
            { builder, _ -> builder },
        onPushMessage: (pushMessage: PushMessage) -> Boolean = { false },
        notificationIdFactory: NotificationIdFactory? = null,
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
            notificationIdFactory = notificationIdFactory,
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
            notificationIdFactory = notificationIdFactory,
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
        val appContext = context.applicationContext
        // Resolved on first use, off SDK initialisation.
        val builder by lazy { coilUserIconBuilder(appContext) ?: DefaultUserIconBuilder(appContext) }
        return object : UserIconBuilder {
            override suspend fun buildIcon(user: User): IconCompat? = builder.buildIcon(user)
        }
    }

    /**
     * Builds ui-common's Coil icon builder, which caches avatars and applies the CDN and image headers. The client
     * can't depend on ui-common, so it's looked up by name; ui-common's consumer rules keep it for R8.
     */
    private fun coilUserIconBuilder(context: Context): UserIconBuilder? = try {
        val builder = Class.forName(COIL_USER_ICON_BUILDER).getConstructor(Context::class.java).newInstance(context)
        logger.d { "[coilUserIconBuilder] using the Coil user icon builder" }
        builder as UserIconBuilder
    } catch (_: ClassNotFoundException) {
        logger.d { "[coilUserIconBuilder] ui-common not found, using the default user icon builder" }
        null
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        // Also linkage errors, so a broken lookup falls back instead of crashing the notification.
        logger.e(e) { "[coilUserIconBuilder] failed to build $COIL_USER_ICON_BUILDER" }
        null
    }

    private const val COIL_USER_ICON_BUILDER =
        "io.getstream.chat.android.ui.common.notifications.StreamCoilUserIconBuilder"

    private val logger by taggedLogger("Chat:NotificationHandlerFactory")
}
