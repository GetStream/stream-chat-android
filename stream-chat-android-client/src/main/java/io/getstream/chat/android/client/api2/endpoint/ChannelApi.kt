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
import io.getstream.chat.android.client.api2.UrlQueryPayload
import io.getstream.chat.android.client.api2.model.requests.PinnedMessagesRequest
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.network.models.GetPinnedMessagesResponse
import io.getstream.chat.android.network.models.Response
import io.getstream.chat.android.network.models.SendEventRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@AuthenticatedApi
internal interface ChannelApi {

    @GET("/api/v2/chat/channels/{type}/{id}/pinned_messages")
    fun getPinnedMessages(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @UrlQueryPayload @Query("payload") payload: PinnedMessagesRequest,
    ): RetrofitCall<GetPinnedMessagesResponse>

    // The generated stopWatchingChannel sends no body, but the backend rejects the request without one.
    @POST("/api/v2/chat/channels/{type}/{id}/stop-watching")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun stopWatching(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<Response>

    /**
     * Sends an event to the channel. The body is returned raw: its `event` is read by the v2 event parser, so its
     * unix-nanosecond dates aren't read as numbers on the way.
     */
    @POST("/api/v2/chat/channels/{type}/{id}/event")
    fun sendEvent(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: SendEventRequest,
    ): RetrofitCall<ResponseBody>
}
