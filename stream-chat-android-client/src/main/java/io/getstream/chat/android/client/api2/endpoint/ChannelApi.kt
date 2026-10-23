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
import io.getstream.chat.android.network.models.ChannelGetOrCreateRequest
import io.getstream.chat.android.network.models.ChannelStateResponse
import io.getstream.chat.android.network.models.DeleteChannelResponse
import io.getstream.chat.android.network.models.GetPinnedMessagesResponse
import io.getstream.chat.android.network.models.GroupedQueryChannelsRequest
import io.getstream.chat.android.network.models.GroupedQueryChannelsResponse
import io.getstream.chat.android.network.models.HideChannelRequest
import io.getstream.chat.android.network.models.MarkDeliveredRequest
import io.getstream.chat.android.network.models.MarkReadRequest
import io.getstream.chat.android.network.models.MarkUnreadRequest
import io.getstream.chat.android.network.models.QueryChannelsRequest
import io.getstream.chat.android.network.models.QueryChannelsResponse
import io.getstream.chat.android.network.models.Response
import io.getstream.chat.android.network.models.SendEventRequest
import io.getstream.chat.android.network.models.TruncateChannelRequest
import io.getstream.chat.android.network.models.TruncateChannelResponse
import io.getstream.chat.android.network.models.UpdateChannelPartialRequest
import io.getstream.chat.android.network.models.UpdateChannelPartialResponse
import io.getstream.chat.android.network.models.UpdateChannelRequest
import io.getstream.chat.android.network.models.UpdateChannelResponse
import io.getstream.chat.android.network.models.UpdateMemberPartialRequest
import io.getstream.chat.android.network.models.UpdateMemberPartialResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("TooManyFunctions")
@AuthenticatedApi
internal interface ChannelApi {

    @POST("/api/v2/chat/channels")
    fun queryChannels(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: QueryChannelsRequest,
    ): RetrofitCall<QueryChannelsResponse>

    /**
     * Queries channels grouped into server-defined groups.
     *
     * Supports per-group request options (limit, next/prev cursors) and returns per-group
     * pagination cursors. Pagination is only allowed when exactly one group is requested.
     *
     * @param connectionId The current connection ID.
     * @param body The request body containing the optional per-group configuration map.
     */
    @POST("/api/v2/chat/channels/grouped")
    fun queryGroupedChannels(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: GroupedQueryChannelsRequest,
    ): RetrofitCall<GroupedQueryChannelsResponse>

    @POST("/api/v2/chat/channels/{type}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: ChannelGetOrCreateRequest,
    ): RetrofitCall<ChannelStateResponse>

    @POST("/api/v2/chat/channels/read")
    fun markAllRead(
        @Body map: Map<String, String> = emptyMap(),
    ): RetrofitCall<Response>

    @POST("/api/v2/chat/channels/{type}/{id}")
    fun updateChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateChannelRequest,
    ): RetrofitCall<UpdateChannelResponse>

    @PATCH("/api/v2/chat/channels/{type}/{id}")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun updateChannelPartial(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateChannelPartialRequest,
    ): RetrofitCall<UpdateChannelPartialResponse>

    @PATCH("/api/v2/chat/channels/{type}/{id}/member")
    fun partialUpdateMember(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateMemberPartialRequest,
    ): RetrofitCall<UpdateMemberPartialResponse>

    @DELETE("/api/v2/chat/channels/{type}/{id}")
    fun deleteChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
    ): RetrofitCall<DeleteChannelResponse>

    @POST("/api/v2/chat/channels/{type}/{id}/hide")
    fun hideChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: HideChannelRequest,
    ): RetrofitCall<Response>

    @POST("/api/v2/chat/channels/{type}/{id}/truncate")
    fun truncateChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: TruncateChannelRequest,
    ): RetrofitCall<TruncateChannelResponse>

    @POST("/api/v2/chat/channels/{type}/{id}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: ChannelGetOrCreateRequest,
    ): RetrofitCall<ChannelStateResponse>

    @POST("/api/v2/chat/channels/{type}/{id}/read")
    fun markRead(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: MarkReadRequest,
    ): RetrofitCall<Response>

    @POST("/api/v2/chat/channels/{type}/{id}/unread")
    fun markUnread(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: MarkUnreadRequest,
    ): RetrofitCall<Response>

    @POST("/api/v2/chat/channels/{type}/{id}/show")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun showChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<Response>

    @POST("/api/v2/chat/channels/{type}/{id}/stop-watching")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun stopWatching(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<Response>

    @GET("/api/v2/chat/channels/{type}/{id}/pinned_messages")
    fun getPinnedMessages(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @UrlQueryPayload @Query("payload") payload: PinnedMessagesRequest,
    ): RetrofitCall<GetPinnedMessagesResponse>

    @POST("/api/v2/chat/channels/delivered")
    fun markDelivered(
        @Body request: MarkDeliveredRequest,
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
