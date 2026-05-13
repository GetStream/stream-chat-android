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
import io.getstream.chat.android.models.GroupedChannelsGroupQuery
import io.getstream.result.Result

/**
 * Listener used when querying grouped channels from the backend.
 */
public interface QueryGroupedChannelsListener {

    /**
     * Called when the query grouped channels request completes.
     *
     * @param result The result of the query grouped channels request.
     * @param limit The request-level default per-group limit, or `null` for the server default.
     * @param groups The per-group request options that were sent, or `null` when the request
     * asked for the server-defined default set of groups.
     * @param watch Whether watching was requested.
     * @param presence Whether presence was requested.
     */
    public suspend fun onQueryGroupedChannelsResult(
        result: Result<GroupedChannels>,
        limit: Int?,
        groups: Map<String, GroupedChannelsGroupQuery>?,
        watch: Boolean,
        presence: Boolean,
    )
}
