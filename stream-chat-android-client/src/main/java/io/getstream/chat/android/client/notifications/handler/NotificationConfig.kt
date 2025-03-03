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

import io.getstream.android.push.PushDeviceGenerator
import io.getstream.chat.android.client.token.TokenProvider

/**
 * Push notifications configuration class
 */
public data class NotificationConfig @JvmOverloads constructor(
    /**
     * Enables/disables push notifications on the device.
     * Device's token won't be registered if push notifications are disabled.
     */
    val pushNotificationsEnabled: Boolean = true,

    /**
     * Push notifications are ignored and not displayed when the user is online (when there is an
     * active WebSocket connection). Set to false if you would like to receive and handle push
     * notifications even if user is online. Default value is true.
     */
    val ignorePushMessagesWhenUserOnline: Boolean = true,

    /**
     * A list of generators responsible for providing the information needed to register a device
     * @see [PushDeviceGenerator]
     */
    val pushDeviceGenerators: List<PushDeviceGenerator> = listOf(),

    /**
     * Allows enabling/disabling showing notification after receiving a push message.
     */
    val shouldShowNotificationOnPush: () -> Boolean = { true },

    /**
     * Allows SDK to request [android.Manifest.permission.POST_NOTIFICATIONS] permission for a connected user.
     */
    val requestPermissionOnAppLaunch: () -> Boolean = { true },

    /**
     * Whether or not the auto-translation feature is enabled.
     */
    val autoTranslationEnabled: Boolean = false,

    /**
     * A token provider to be used on case of restoring user credentials and an expired token needs to be refreshed.
     * If not provided, the SDK will create a [TokenProvider] with the last token used within the client.
     */
    val tokenProvider: TokenProvider? = null,

)
