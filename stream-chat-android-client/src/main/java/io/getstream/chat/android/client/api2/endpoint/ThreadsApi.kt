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

package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateThreadRequest
import io.getstream.chat.android.client.api2.model.requests.QueryThreadsRequest
import io.getstream.chat.android.client.api2.model.response.QueryThreadsResponse
import io.getstream.chat.android.client.api2.model.response.ThreadInfoResponse
import io.getstream.chat.android.client.api2.model.response.ThreadResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

@AuthenticatedApi
internal interface ThreadsApi {

    /**
     * [REST documentation](https://getstream.io/chat/docs/rest/#product:chat-querythreads)
     */
    @POST("/threads")
    fun queryThreads(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: QueryThreadsRequest,
    ): RetrofitCall<QueryThreadsResponse>

    /**
     * [REST documentation](https://getstream.io/chat/docs/rest/#product:chat-getthread)
     */
    @GET("/threads/{message_id}")
    fun getThread(
        @Path("message_id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @QueryMap options: Map<String, String>,
    ): RetrofitCall<ThreadResponse>

    /**
     * [REST documentation](https://getstream.github.io/protocol/#/product%3Achat/UpdateThreadPartial)
     */
    @PATCH("/threads/{message_id}")
    fun partialUpdateThread(
        @Path("message_id") messageId: String,
        @Body body: PartialUpdateThreadRequest,
    ): RetrofitCall<ThreadInfoResponse>
}
