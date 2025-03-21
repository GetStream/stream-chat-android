/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.UserBlock
import io.getstream.result.Result

/**
 * Listener for [ChatClient.queryBlockedUsers] requests.
 */
public interface QueryBlockedUsersListener {

    /**
     * Runs side effect after the request was completed.
     *
     * @param result The [Result] containing the successfully retrieved list of blocked users or the error.
     */
    public fun onQueryBlockedUsersResult(result: Result<List<UserBlock>>)
}
