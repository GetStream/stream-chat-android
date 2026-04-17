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

import io.getstream.chat.android.models.GroupedChannels
import io.getstream.result.Result

/**
 * Listener used when querying grouped channels from the backend.
 */
public interface GroupedQueryChannelsListener {

    /**
     * Called when the grouped query channels request completes.
     *
     * @param result The result of the grouped query channels request.
     * @param limit The maximum number of channels per group that was requested.
     * @param watch Whether watching was requested.
     * @param presence Whether presence was requested.
     */
    public suspend fun onGroupedQueryChannelsResult(
        result: Result<GroupedChannels>,
        limit: Int?,
        watch: Boolean,
        presence: Boolean,
    )
}
