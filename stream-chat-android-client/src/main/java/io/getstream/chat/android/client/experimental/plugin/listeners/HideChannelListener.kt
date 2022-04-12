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
import io.getstream.chat.android.client.utils.Result

/**
 * Listener of [ChatClient.hideChannel] requests.
 */
public interface HideChannelListener {

    /**
     * Run precondition for the request. If it returns [Result.isSuccess] then the request is run otherwise it returns
     * [Result.error] and no request is made.
     *
     * @param channelType Type of the requested channel.
     * @param channelId Id of the requested channel.
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     *
     * @return [Result.success] if precondition passes otherwise [Result.error]
     */
    public suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean
    ): Result<Unit>

    /**
     * Runs side effect before the request is launched.
     *
     * @param channelType Type of the requested channel.
     * @param channelId Id of the requested channel.
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     */
    public suspend fun onHideChannelRequest(
        channelType: String,
        channelId: String,
        clearHistory: Boolean
    )

    /**
     * Runs this function on the result of the request.
     *
     * @param result Result of this request.
     * @param channelType Type of the requested channel.
     * @param channelId Id of the requested channel.
     * @param clearHistory Boolean, if you want to clear the history of this channel or not.
     */
    public suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    )
}
