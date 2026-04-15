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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import io.getstream.result.Result
import java.util.Date

/**
 * Listener for push notification preference changes.
 */
@InternalStreamChatApi
public interface PushPreferencesListener {

    /**
     * Called when the push notification preference is updated for a given channel.
     *
     * @param cid The full channel ID (e.g., "messaging:123").
     * @param level The level at which the preference is set (user, channel, or channel group).
     * @param result The result of the update operation, containing the updated [PushPreference] on success or an error
     * on failure.
     */
    public suspend fun onChannelPushPreferenceSet(
        cid: String,
        level: PushPreferenceLevel,
        result: Result<PushPreference>,
    )

    /**
     * Called when push notifications are snoozed for a given channel until a specified time.
     *
     * @param cid The full channel ID (e.g., "messaging:123").
     * @param until The [Date] until which push notifications are snoozed.
     * @param result The result of the snooze operation, containing the updated [PushPreference] on success or an error
     * on failure.
     */
    public suspend fun onChannelPushNotificationsSnoozed(
        cid: String,
        until: Date,
        result: Result<PushPreference>,
    )
}
