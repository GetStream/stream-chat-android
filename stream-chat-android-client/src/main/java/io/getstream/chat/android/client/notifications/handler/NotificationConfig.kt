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

/**
 * Push notifications configuration class
 */
public data class NotificationConfig(
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
