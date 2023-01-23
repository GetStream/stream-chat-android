/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import androidx.core.graphics.drawable.IconCompat
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.notifications.permissions.DefaultNotificationPermissionHandler
import io.getstream.chat.android.client.notifications.permissions.NotificationPermissionHandler
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
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
     * @param newMessageIntent Lambda expression used to generate an [Intent] to open your app
     * @param notificationChannel Lambda expression used to generate a [NotificationChannel].
     * Used in SDK_INT >= VERSION_CODES.O.
     * @param userIconBuilder Generates [IconCompat] to be shown on notifications.
     * @param permissionHandler Handles [android.Manifest.permission.POST_NOTIFICATIONS] permission lifecycle.
     */
    @SuppressLint("NewApi")
    @JvmOverloads
    @JvmStatic
    public fun createNotificationHandler(
        context: Context,
        newMessageIntent: ((message: Message, channel: Channel) -> Intent)? = null,
        notificationChannel: (() -> NotificationChannel)? = null,
        userIconBuilder: UserIconBuilder = provideDefaultUserIconBuilder(context),
        permissionHandler: NotificationPermissionHandler? = provideDefaultNotificationPermissionHandler(context),
    ): NotificationHandler {
        val notificationChannelFun = notificationChannel ?: getDefaultNotificationChannel(context)
        (newMessageIntent ?: getDefaultNewMessageIntentFun(context)).let { newMessageIntentFun ->
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MessagingStyleNotificationHandler(
                    context,
                    newMessageIntentFun,
                    notificationChannelFun,
                    userIconBuilder,
                    permissionHandler
                )
            } else {
                ChatNotificationHandler(context, newMessageIntentFun, notificationChannelFun)
            }
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
        return runCatching {
            Class.forName("io.getstream.chat.android.ui.common.notifications.StreamCoilUserIconBuilder")
                .kotlin.primaryConstructor
                ?.call(appContext) as UserIconBuilder
        }.getOrDefault(DefaultUserIconBuilder(appContext))
    }

    private fun provideDefaultNotificationPermissionHandler(context: Context): NotificationPermissionHandler {
        val appContext = context.applicationContext
        return runCatching {
            Class.forName(
                "io.getstream.chat.android.ui.common.notifications.permissions.SnackbarNotificationPermissionHandler"
            ).kotlin.primaryConstructor?.call(appContext) as NotificationPermissionHandler
        }.getOrDefault(DefaultNotificationPermissionHandler(appContext))
    }
}
