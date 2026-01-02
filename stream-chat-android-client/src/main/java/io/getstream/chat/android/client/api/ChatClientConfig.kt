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

package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.internal.DistinctChatApi
import io.getstream.chat.android.client.logger.ChatLoggerConfig
import io.getstream.chat.android.client.notifications.handler.NotificationConfig

/**
 * A config to setup the [ChatClient] behavior.
 *
 * @param apiKey The API key of your Stream Chat app obtained from the
 * [Stream Dashboard](https://dashboard.getstream.io/).
 * @param httpUrl The base URL to be used by the client.
 * @param cdnHttpUrl The base CDN URL to be used by the client.
 * @param wssUrl The base WebSocket URL to be used by the client.
 * @param warmUp Controls the connection warm-up behavior.
 * @param loggerConfig A logging config to be used by the client.
 * @param distinctApiCalls Controls whether [DistinctChatApi] is enabled or not.
 * @param debugRequests Controls whether requests can be recorded or not.
 * @param notificationConfig A notification config to be used by the client.
 */
@Suppress("LongParameterList")
public class ChatClientConfig @JvmOverloads constructor(
    public val apiKey: String,
    public var httpUrl: String,
    public var cdnHttpUrl: String,
    public var wssUrl: String,
    public val warmUp: Boolean,
    public val loggerConfig: ChatLoggerConfig,
    public var distinctApiCalls: Boolean = true,
    public val debugRequests: Boolean,
    public val notificationConfig: NotificationConfig,
) {
    public var isAnonymous: Boolean = false
}
