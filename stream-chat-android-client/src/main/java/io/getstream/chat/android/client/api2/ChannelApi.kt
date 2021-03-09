package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api2.model.requests.AcceptInviteRequest
import io.getstream.chat.android.client.api2.model.requests.AddMembersRequest
import io.getstream.chat.android.client.api2.model.requests.HideChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MarkReadRequest
import io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest
import io.getstream.chat.android.client.api2.model.requests.QueryChannelsRequest
import io.getstream.chat.android.client.api2.model.requests.RejectInviteRequest
import io.getstream.chat.android.client.api2.model.requests.RemoveMembersRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateChannelRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateCooldownRequest
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.QueryChannelsResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@MoshiApi
@AuthenticatedApi
internal interface ChannelApi {

    @GET("/channels")
    fun queryChannels(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") clientID: String,
        @UrlQueryPayload @Query("payload") payload: QueryChannelsRequest,
    ): RetrofitCall<QueryChannelsResponse>

    @POST("/channels/{type}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") clientID: String,
        @Body request: QueryChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/read")
    fun markAllRead(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}")
    fun updateChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: UpdateChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun updateCooldown(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: UpdateCooldownRequest,
    ): RetrofitCall<ChannelResponse>

    @DELETE("/channels/{type}/{id}")
    fun deleteChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun acceptInvite(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: AcceptInviteRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun rejectInvite(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: RejectInviteRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun addMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Body body: AddMembersRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun removeMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Body body: RemoveMembersRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/hide")
    fun hideChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: HideChannelRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") clientID: String,
        @Body request: QueryChannelRequest,
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/read")
    fun markRead(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body request: MarkReadRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/show")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun showChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/stop-watching")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun stopWatching(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: Map<Any, Any>,
    ): RetrofitCall<CompletableResponse>
}
