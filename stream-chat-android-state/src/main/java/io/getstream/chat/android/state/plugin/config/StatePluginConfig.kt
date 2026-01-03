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

package io.getstream.chat.android.state.plugin.config

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.TimeDuration
import io.getstream.chat.android.state.extensions.queryChannelsAsState
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.state.plugin.internal.StatePlugin

/**
 * Provides a configuration for [io.getstream.chat.android.state.plugin.internal.StatePlugin].
 *
 * @param backgroundSyncEnabled Controls whether the SDK performs background synchronization when push notifications
 * are received. When enabled (default: `true`), the SDK automatically syncs messages in the background when a push
 * notification arrives, ensuring the local state/database stays up-to-date even when the app is in the background.
 * This is particularly useful for displaying accurate notification content and maintaining
 * offline state consistency. Disable this if you want to reduce background processing.
 *
 * @param userPresence Controls whether the SDK subscribes to and processes user presence events (online/offline status,
 * last active time). When enabled (default: `true`), the SDK receives real-time updates about user presence changes
 * and updates the user objects in channels, members, and watchers accordingly. This affects both WebSocket event
 * subscriptions and the `presence` parameter in API requests. Disabling this can reduce network traffic and processing
 * overhead if your application doesn't need to display user online/offline status.
 *
 * @param isAutomaticSyncOnReconnectEnabled Specifies if local data is updated with subscribing to web-socket events
 * after reconnection.
 * When turning this off, it is up for SDK user to observe web-socket connection state for
 * reconnection and syncing data by calling, for example [ChatClient.queryChannels], or the [StatePlugin] methods such
 * as [ChatClient.queryChannelsAsState] / [ChatClient.watchChannelAsState]. Note that these calls fetch the latest
 * state and also subscribe to web-socket events if the query's `watch` is set to true.
 *
 * Web-socket connection status can be monitored by observing the [ClientState.connectionState] flow, exposed from the
 * [ChatClient.clientState]:
 * ```kotlin
 * client.clientState.connectionState.collectLatest { state ->
 *     when (state) {
 *         is ConnectionState.Connected -> {
 *             // Data can be updated and watching can be resumed
 *         }
 *         else -> {}
 *     }
 * }
 * ```
 *
 * @param syncMaxThreshold The maximum age threshold for pending local operations (channels, messages, reactions)
 * before they are considered too old to retry and are discarded. Default is 12 hours. When the SDK attempts to
 * retry failed operations (e.g., sending a message, creating a channel, adding a reaction) upon reconnection, it
 * checks if the operation's timestamp (createdLocallyAt, updatedLocallyAt, deletedAt, or createdAt) exceeds this
 * threshold. If it does, the operation is removed from the local database instead of being retried, preventing
 * the SDK from attempting to sync stale operations that are no longer relevant.
 *
 * @param now A function that provides the current time in milliseconds since epoch (Unix timestamp). Defaults to
 * [System.currentTimeMillis]. This is used throughout the state plugin for time-based operations such as:
 * - Determining if pinned messages have expired (based on `pinExpires` timestamp)
 * - Calculating sync thresholds and time differences
 * - Timestamping state updates
 * This parameter primarily exists for testing purposes, allowing tests to inject a controlled time source, but can
 * also be used in production if you need custom time handling (e.g., using a synchronized server time).
 *
 * @param messageLimitConfig Configuration that controls the maximum number of messages kept in memory for different
 * channel types. This helps manage memory usage in channels with large message histories. When the number of messages
 * exceeds the configured limit (plus a buffer), older messages are automatically trimmed from the in-memory state.
 * By default, no limits are applied, meaning all messages are kept in memory. See [MessageLimitConfig] and
 * [ChannelMessageLimit] for configuration details.
 *
 * @param useLegacyChannelLogic When set to true, the SDK uses the legacy channel state management logic for
 * handling channel updates and events. This may be necessary for compatibility with existing implementations.
 * When set to false, the SDK employs the new channel state management logic, which includes optimizations and
 * performance improvements. Default is true.
 */
public data class StatePluginConfig @JvmOverloads constructor(
    @Deprecated(
        "The background sync on push notification is no longer needed to keep the state in sync and " +
            "will be removed in the future. If you are using the default UI components, or building your own UI " +
            "using [ChatClient.queryChannelsAsState] / [ChatClient.watchChannelAsState], the state will always be " +
            "up-to-date. We recommend disabling it to avoid unnecessary background work.",
    )
    public val backgroundSyncEnabled: Boolean = true,
    public val userPresence: Boolean = true,
    public val isAutomaticSyncOnReconnectEnabled: Boolean = true,
    public val syncMaxThreshold: TimeDuration = TimeDuration.hours(12),
    public val now: () -> Long = { System.currentTimeMillis() },
    public val messageLimitConfig: MessageLimitConfig = MessageLimitConfig(),
    public val useLegacyChannelLogic: Boolean = true,
)

/**
 * Configuration for message limits in channels.
 *
 * This configuration allows you to control memory usage by limiting the number of messages kept in the in-memory
 * state for different channel types. When a channel exceeds its configured message limit (plus a 30-message buffer
 * to avoid frequent trimming), the SDK automatically removes the oldest messages from memory while keeping the most
 * recent ones.
 *
 * Message trimming behavior:
 * - Only applies when not loading older messages (to avoid interfering with pagination)
 * - Sorts messages by creation time and keeps the most recent ones
 * - Sets `endOfOlderMessages` to `false` when trimming occurs (indicating more messages exist)
 * - Messages are only removed from in-memory state, not from the local database
 *
 * Use cases:
 * - Limit memory usage in channels with extensive message history
 * - Optimize performance in resource-constrained environments
 * - Configure different limits for different channel types (e.g., smaller limits for group channels, larger for DMs)
 *
 * Example configuration:
 * ```kotlin
 * StatePluginConfig(
 *     messageLimitConfig = MessageLimitConfig(
 *         channelMessageLimits = setOf(
 *             ChannelMessageLimit(channelType = "messaging", baseLimit = 1000),
 *             ChannelMessageLimit(channelType = "livestream", baseLimit = 500)
 *         )
 *     )
 * )
 * ```
 *
 * @param channelMessageLimits A set of [ChannelMessageLimit] defining the maximum number of messages to keep in
 * memory for different channel types. By default, this is an empty set, meaning no limits are applied and all
 * messages are kept in memory. Each channel type can have its own limit configured independently.
 */
public data class MessageLimitConfig(
    public val channelMessageLimits: Set<ChannelMessageLimit> = setOf(),
)

/**
 * Defines a message limit for a specific channel type.
 *
 * This class specifies the maximum number of messages to keep in memory for channels of a particular type.
 * When the number of messages in a channel exceeds this limit (plus a 30-message buffer), the SDK automatically
 * trims older messages from the in-memory state, keeping only the most recent messages up to the limit.
 *
 * Example usage:
 * ```kotlin
 * // Limit messaging channels to 100 messages in memory
 * ChannelMessageLimit(channelType = "messaging", baseLimit = 1000)
 *
 * // Limit livestream channels to 50 messages in memory
 * ChannelMessageLimit(channelType = "livestream", baseLimit = 500)
 * ```
 *
 * @param channelType The type of the channel for which this limit applies (e.g., "messaging", "livestream",
 * "commerce"). This should match the channel type used when creating channels. Channel types not specified in the
 * configuration will have no message limits applied.
 *
 * @param baseLimit The maximum number of messages to keep in memory for channels of this type. When the message
 * count exceeds `baseLimit + 30` (the buffer), the SDK trims messages down to this limit. Must be a positive integer.
 * The 30-message buffer is included to avoid trimming too frequently during normal message flow.
 */
public data class ChannelMessageLimit(
    public val channelType: String,
    public val baseLimit: Int,
)
