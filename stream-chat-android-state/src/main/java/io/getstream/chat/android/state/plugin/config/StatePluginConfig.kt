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

package io.getstream.chat.android.state.plugin.config

import io.getstream.chat.android.models.TimeDuration

/**
 * Provides a configuration for [io.getstream.chat.android.state.plugin.internal.StatePlugin].
 *
 * @param backgroundSyncEnabled Whether the SDK should perform background sync if some queries fail.
 * @param userPresence Whether the SDK should receive user presence changes.
 * @param syncMaxThreshold The maximum time allowed for data to synchronize. If not synced within this limit, the SDK deletes it.
 * @param now A function that provides the current time in milliseconds.
 * @param messageLimitConfig Configuration for message limits in channels.
 */
public data class StatePluginConfig @JvmOverloads constructor(
    public val backgroundSyncEnabled: Boolean = true,
    public val userPresence: Boolean = true,
    public val syncMaxThreshold: TimeDuration = TimeDuration.hours(12),
    public val now: () -> Long = { System.currentTimeMillis() },
    public val messageLimitConfig: MessageLimitConfig = MessageLimitConfig(),
)

/**
 * Configuration for message limits in channels.
 *
 * @param channelTypes The set of channel types for which the limit applies.
 * @param limit The maximum number of messages to keep in memory for the channel.
 */
public data class MessageLimitConfig(
    public val channelTypes: Set<String> = setOf("livestream"),
    public val limit: Int = 1000,
)
