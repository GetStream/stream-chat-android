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
import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/** Listener for reply queries. */
public interface ThreadQueryListener {
    /**
     * Runs precondition check for [ChatClient.getReplies], [ChatClient.getNewerReplies] and
     * [ChatClient.getRepliesMore]. If it returns [Result.Success] then the request is run
     * otherwise it returns [Result.Failure] and no request is made.
     *
     * @param parentId The id of the parent message.
     *
     * @return [Result.Success] if the precondition is met, [Result.Failure] otherwise.
     */
    public suspend fun onGetRepliesPrecondition(parentId: String): Result<Unit> = Result.Success(Unit)

    /** Runs side effect before the request is launched. */
    public suspend fun onGetRepliesRequest(parentId: String, limit: Int)

    /** Runs this function on the result of the [ChatClient.getReplies] request. */
    public suspend fun onGetRepliesResult(result: Result<List<Message>>, parentId: String, limit: Int)

    /** Runs side effect before the request is launched. */
    public suspend fun onGetRepliesMoreRequest(
        parentId: String,
        firstId: String,
        limit: Int,
    )

    /** Runs this function on the result of the [ChatClient.getRepliesMore] request. */
    public suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        parentId: String,
        firstId: String,
        limit: Int,
    )

    /** Runs side effect before the request is launched. */
    public suspend fun onGetNewerRepliesRequest(
        parentId: String,
        limit: Int,
        lastId: String?,
    )

    /** Runs this function on the result of the [ChatClient.getNewerReplies] request. */
    public suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    )
}
