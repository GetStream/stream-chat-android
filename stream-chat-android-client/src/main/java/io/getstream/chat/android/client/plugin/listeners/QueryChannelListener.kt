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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.models.Channel
import io.getstream.result.Result

/**
 * Listener of [ChatClient.queryChannel] requests.
 */
public interface QueryChannelListener {
    /**
     * Run precondition for the request. If it returns [Result.Success] then the request is run otherwise it returns
     * [Result.Failure] and no request is made.
     */
    public suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit>

    /**
     * Runs side effect before the request is launched.
     *
     * @param channelType Type of the requested channel.
     * @param channelId Id of the requested channel.
     * @param request [QueryChannelRequest] which is going to be used for the request.
     */
    public suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    )

    /**
     * Runs this function on the result of the request.
     */
    public suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    )
}
