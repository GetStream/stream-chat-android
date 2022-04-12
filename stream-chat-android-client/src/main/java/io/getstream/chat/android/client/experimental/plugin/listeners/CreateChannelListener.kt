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

package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result

/**
 * Listener for [io.getstream.chat.android.client.ChatClient.createChannel] calls.
 */
public interface CreateChannelListener {

    /**
     * A method called before making an API call to create a channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     * @param extraData Map of key-value pairs that let you store extra data
     * @param currentUser The currently logged in user.
     */
    public suspend fun onCreateChannelRequest(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
        currentUser: User,
    )

    /**
     * A method called after receiving the response from the create channel call.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     * @param result The API call result.
     */
    public suspend fun onCreateChannelResult(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        result: Result<Channel>,
    )

    /**
     * Runs precondition check for [ChatClient.createChannel].
     * The request will be run if the method returns [Result.success] and won't be made if it returns [Result.error].
     *
     * @param currentUser The currently logged in user.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     *
     * @return [Result.success] if the precondition is fulfilled, [Result.error] otherwise.
     */
    public fun onCreateChannelPrecondition(
        currentUser: User?,
        channelId: String,
        memberIds: List<String>,
    ): Result<Unit>
}
