package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.AcceptInviteRequest
import io.getstream.chat.android.client.api.models.AddDeviceRequest
import io.getstream.chat.android.client.api.models.AddMembersRequest
import io.getstream.chat.android.client.api.models.BanUserRequest
import io.getstream.chat.android.client.api.models.ChannelResponse
import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.EventResponse
import io.getstream.chat.android.client.api.models.FlagResponse
import io.getstream.chat.android.client.api.models.GetDevicesResponse
import io.getstream.chat.android.client.api.models.GetReactionsResponse
import io.getstream.chat.android.client.api.models.GetRepliesResponse
import io.getstream.chat.android.client.api.models.GetSyncHistory
import io.getstream.chat.android.client.api.models.GetSyncHistoryResponse
import io.getstream.chat.android.client.api.models.HideChannelRequest
import io.getstream.chat.android.client.api.models.MarkReadRequest
import io.getstream.chat.android.client.api.models.MessageRequest
import io.getstream.chat.android.client.api.models.MessageResponse
import io.getstream.chat.android.client.api.models.MuteChannelRequest
import io.getstream.chat.android.client.api.models.MuteUserRequest
import io.getstream.chat.android.client.api.models.MuteUserResponse
import io.getstream.chat.android.client.api.models.QueryBannedUsersRequest
import io.getstream.chat.android.client.api.models.QueryBannedUsersResponse
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryChannelsResponse
import io.getstream.chat.android.client.api.models.QueryMembersRequest
import io.getstream.chat.android.client.api.models.QueryMembersResponse
import io.getstream.chat.android.client.api.models.QueryUserListResponse
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.ReactionRequest
import io.getstream.chat.android.client.api.models.ReactionResponse
import io.getstream.chat.android.client.api.models.RejectInviteRequest
import io.getstream.chat.android.client.api.models.RemoveMembersRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.api.models.SearchMessagesResponse
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.SendEventRequest
import io.getstream.chat.android.client.api.models.TranslateMessageRequest
import io.getstream.chat.android.client.api.models.UpdateChannelRequest
import io.getstream.chat.android.client.api.models.UpdateCooldownRequest
import io.getstream.chat.android.client.api.models.UpdateUsersRequest
import io.getstream.chat.android.client.api.models.UpdateUsersResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser.UrlQueryPayload
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.OPTIONS
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@AuthenticatedApi
internal interface RetrofitApi {

    // region channels

    @POST("/moderation/mute/channel")
    fun muteChannel(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("connection_id") connectionId: String,
        @Body body: MuteChannelRequest
    ): RetrofitCall<CompletableResponse>

    @POST("/moderation/unmute/channel")
    fun unmuteChannel(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("connection_id") connectionId: String,
        @Body body: MuteChannelRequest
    ): RetrofitCall<CompletableResponse>

    @GET("/channels")
    fun queryChannels(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") clientID: String,
        @UrlQueryPayload @Query("payload") payload: QueryChannelsRequest
    ): RetrofitCall<QueryChannelsResponse>

    @POST("/channels/{type}/{id}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") clientID: String,
        @Body request: QueryChannelRequest
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/query")
    fun queryChannel(
        @Path("type") channelType: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") clientID: String,
        @Body request: QueryChannelRequest
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun updateChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: UpdateChannelRequest
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun updateCooldown(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: UpdateCooldownRequest
    ): RetrofitCall<ChannelResponse>

    @DELETE("/channels/{type}/{id}")
    fun deleteChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/stop-watching")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun stopWatching(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: Map<Any, Any>
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}")
    fun acceptInvite(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: AcceptInviteRequest
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun rejectInvite(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: RejectInviteRequest
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}/hide")
    fun hideChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: HideChannelRequest
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/show")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun showChannel(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: Map<Any, Any>
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/read")
    fun markRead(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body request: MarkReadRequest
    ): RetrofitCall<EventResponse>

    @POST("/channels/read")
    fun markAllRead(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): RetrofitCall<CompletableResponse>

    @POST("/channels/{type}/{id}/event")
    fun sendEvent(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body request: SendEventRequest
    ): RetrofitCall<EventResponse>

    // endregion

    //region users

    @POST("/users")
    fun updateUsers(
        @Query("api_key") apiKey: String,
        @Query("connection_id") connectionId: String,
        @Body body: UpdateUsersRequest
    ): RetrofitCall<UpdateUsersResponse>

    @GET("/users")
    fun queryUsers(
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryUsersRequest
    ): RetrofitCall<QueryUserListResponse>

    @POST("/channels/{type}/{id}")
    fun addMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Body body: AddMembersRequest
    ): RetrofitCall<ChannelResponse>

    @POST("/channels/{type}/{id}")
    fun removeMembers(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Body body: RemoveMembersRequest
    ): RetrofitCall<ChannelResponse>

    @GET("/members")
    fun queryMembers(
        @Query("api_key") apiKey: String,
        @Query("connection_id") connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryMembersRequest
    ): RetrofitCall<QueryMembersResponse>

    @POST("/moderation/mute")
    fun muteUser(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body body: MuteUserRequest
    ): RetrofitCall<MuteUserResponse>

    @POST("/moderation/unmute")
    fun unmuteUser(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body body: MuteUserRequest
    ): RetrofitCall<CompletableResponse>

    @POST("/moderation/flag")
    fun flag(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body body: Map<String, String>
    ): RetrofitCall<FlagResponse>

    @POST("/moderation/unflag")
    fun unflag(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body body: Map<String, String>
    ): RetrofitCall<FlagResponse>

    @POST("/moderation/ban")
    fun banUser(
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Body body: BanUserRequest
    ): RetrofitCall<CompletableResponse>

    @DELETE("/moderation/ban")
    fun unbanUser(
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Query("target_user_id") targetUserId: String,
        @Query("type") channelType: String,
        @Query("id") channelId: String,
        @Query("shadow") shadow: Boolean,
    ): RetrofitCall<CompletableResponse>

    @GET("/query_banned_users")
    fun queryBannedUsers(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("connection_id") connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryBannedUsersRequest,
    ): RetrofitCall<QueryBannedUsersResponse>

    //endregion

    //region messages

    @POST("/channels/{type}/{id}/message")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun sendMessage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body message: MessageRequest
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun updateMessage(
        @Path("id") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body message: MessageRequest
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{id}")
    fun getMessage(
        @Path("id") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}/action")
    fun sendAction(
        @Path("id") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body request: SendActionRequest
    ): RetrofitCall<MessageResponse>

    @DELETE("/messages/{id}")
    fun deleteMessage(
        @Path("id") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}/reaction")
    fun sendReaction(
        @Path("id") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body request: ReactionRequest
    ): RetrofitCall<ReactionResponse>

    @DELETE("/messages/{id}/reaction/{type}")
    fun deleteReaction(
        @Path("id") messageId: String,
        @Path("type") reactionType: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{id}/reactions")
    fun getReactions(
        @Path("id") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): RetrofitCall<GetReactionsResponse>

    @GET("/messages/{parent_id}/replies")
    fun getReplies(
        @Path("parent_id") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Query("limit") limit: Int
    ): RetrofitCall<GetRepliesResponse>

    @GET("/messages/{parent_id}/replies")
    fun getRepliesMore(
        @Path("parent_id") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Query("limit") limit: Int,
        @Query("id_lt") firstId: String
    ): RetrofitCall<GetRepliesResponse>

    //endregion

    //region search

    @GET("/search")
    fun searchMessages(
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @UrlQueryPayload @Query("payload") payload: SearchMessagesRequest
    ): RetrofitCall<SearchMessagesResponse>

    //endregion

    // region Device
    @GET("/devices")
    fun getDevices(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): RetrofitCall<GetDevicesResponse>

    @POST("devices")
    fun addDevices(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body request: AddDeviceRequest
    ): RetrofitCall<CompletableResponse>

    @DELETE("/devices")
    fun deleteDevice(
        @Query("id") deviceId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): RetrofitCall<CompletableResponse>

    // endregion

    @POST("/messages/{messageId}/translate")
    fun translate(
        @Path("messageId") messageId: String,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("connection_id") connectionId: String,
        @Body request: TranslateMessageRequest
    ): RetrofitCall<MessageResponse>

    @POST("/sync")
    fun getSyncHistory(
        @Body body: GetSyncHistory,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("connection_id") connectionId: String
    ): RetrofitCall<GetSyncHistoryResponse>

    @OPTIONS("/connect")
    fun warmUp(): RetrofitCall<ResponseBody>
}
