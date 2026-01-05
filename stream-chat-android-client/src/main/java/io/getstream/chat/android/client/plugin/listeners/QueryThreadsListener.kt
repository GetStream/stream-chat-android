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
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.result.Result

/**
 * Listener for [ChatClient.queryChannels] requests.
 */
public interface QueryThreadsListener {

    /**
     * Run precondition for the request. If it returns [Result.Success] then the request is run otherwise it returns
     * [Result.Failure] and no request is made.
     *
     * @param request [QueryThreadsRequest] which is going to be used for the request.
     *
     * @return [Result.Success] if precondition passes otherwise [Result.Failure]
     */
    public suspend fun onQueryThreadsPrecondition(request: QueryThreadsRequest): Result<Unit>

    /**
     * Runs side effect before the request is launched.
     *
     * @param request [QueryThreadsRequest] which is going to be used for the request.
     */
    public suspend fun onQueryThreadsRequest(request: QueryThreadsRequest)

    /**
     * Runs side effect the request was completed.
     *
     * @param result The [Result] containing the successfully retrieved [QueryThreadsResult] or the error.
     * @param request [QueryThreadsRequest] which is was used for the request.
     */
    public suspend fun onQueryThreadsResult(result: Result<QueryThreadsResult>, request: QueryThreadsRequest)
}
