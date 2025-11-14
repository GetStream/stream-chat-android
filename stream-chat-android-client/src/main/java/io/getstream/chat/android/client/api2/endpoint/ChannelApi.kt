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

package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.UrlQueryPayload
import io.getstream.chat.android.client.api2.model.requests.AcceptInviteRequest
import io.getstream.chat.android.client.api2.model.requests.AddMembersRequest
import io.getstream.chat.android.client.api2.model.requests.HideChannelRequest
import io.getstream.chat.android.client.api2.model.requests.InviteMembersRequest
import io.getstream.chat.android.client.api2.model.requests.MarkDeliveredRequest
import io.getstream.chat.android.client.api2.model.requests.MarkReadRequest
import io.getstream.chat.android.client.api2.model.requests.MarkUnreadRequest
import io.getstream.chat.android.client.api2.model.requests.PinnedMessagesRequest
import io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest
import io.getstream.chat.android.client.api2.model.requests.QueryChannelsRequest
import io.getstream.chat.android.client.api2.model.requests.RejectInviteRequest
import io.getstream.chat.android.client.api2.model.requests.RemoveMembersRequest
import io.getstream.chat.android.client.api2.model.requests.SendEventRequest
import io.getstream.chat.android.client.api2.model.requests.TruncateChannelRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelPartialRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateCooldownRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMemberPartialRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMemberPartialResponse
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.EventResponse
import io.getstream.chat.android.client.api2.model.response.MessagesResponse
import io.getstream.chat.android.client.api2.model.response.QueryChannelsResponse
import io.getstream.chat.android.client.call.RetrofitCall
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

    @POST("/channels")
    fun queryChannels(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: QueryChannelsRequest,
    ): RetrofitCall<QueryChannelsResponse>

    @POST("/channels/{type}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: QueryChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/read")
    fun markAllRead(
        @Body map: Map<String, String> = emptyMap(),
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}")
    fun updateChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @PATCH("/channels/{type}/{id}")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun updateChannelPartial(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateChannelPartialRequest,
    ): RetrofitCall<ChannelResponse>

    @PATCH("/channels/{type}/{id}")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun updateCooldown(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: UpdateCooldownRequest,
    ): RetrofitCall<ChannelResponse>

    @DELETE("/channels/{type}/{id}")
    fun deleteChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun acceptInvite(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: AcceptInviteRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun rejectInvite(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: RejectInviteRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun addMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: AddMembersRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun removeMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: RemoveMembersRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun inviteMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: InviteMembersRequest,
    ): RetrofitCall<ChannelResponse>

    @PATCH("/channels/{type}/{id}/member/{user_id}")
    fun partialUpdateMember(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Path("user_id") userId: String,
        @Body body: UpdateMemberPartialRequest,
    ): RetrofitCall<UpdateMemberPartialResponse>

    @POST("/channels/{type}/{id}/event")
    fun sendEvent(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: SendEventRequest,
    ): RetrofitCall<EventResponse>

    @POST("/channels/{type}/{id}/hide")
    fun hideChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: HideChannelRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/truncate")
    fun truncateChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: TruncateChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: QueryChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/read")
    fun markRead(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: MarkReadRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/unread")
    fun markUnread(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body request: MarkUnreadRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/show")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun showChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/stop-watching")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun stopWatching(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<CompletableResponse>

    @GET("/channels/{type}/{id}/pinned_messages")
    fun getPinnedMessages(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @UrlQueryPayload @Query("payload") payload: PinnedMessagesRequest,
    ): RetrofitCall<MessagesResponse>

    @POST("/channels/delivered")
    fun markDelivered(
        @Body request: MarkDeliveredRequest,
    ): RetrofitCall<CompletableResponse>
}
