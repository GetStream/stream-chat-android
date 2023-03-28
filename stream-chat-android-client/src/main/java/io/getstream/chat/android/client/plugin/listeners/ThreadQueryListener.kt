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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/** Listener for reply queries. */
public interface ThreadQueryListener {
    /**
     * Runs precondition check for [ChatClient.getReplies]. If it returns [Result.Success] then the request is run
     * otherwise it returns [Result.Failure] and no request is made.
     */
    public suspend fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> =
        Result.Success(Unit)

    /** Runs side effect before the request is launched. */
    public suspend fun onGetRepliesRequest(messageId: String, limit: Int)

    /** Runs this function on the result of the [ChatClient.getReplies] request. */
    public suspend fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int)

    /**
     * Runs precondition check for [ChatClient.getRepliesMore]. If it returns [Result.Success] then the request is run
     * otherwise it returns [Result.Failure] and no request is made.
     */
    public suspend fun onGetRepliesMorePrecondition(
        messageId: String,
        firstId: String,
        limit: Int,
    ): Result<Unit> = Result.Success(Unit)

    /** Runs side effect before the request is launched. */
    public suspend fun onGetRepliesMoreRequest(
        messageId: String,
        firstId: String,
        limit: Int,
    )

    /** Runs this function on the result of the [ChatClient.getRepliesMore] request. */
    public suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    )
}
