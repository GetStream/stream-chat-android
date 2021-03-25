package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.model.requests.BanUserRequest
import io.getstream.chat.android.client.api2.model.requests.MuteChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MuteUserRequest
import io.getstream.chat.android.client.api2.model.requests.QueryBannedUsersRequest
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.client.api2.model.response.MuteUserResponse
import io.getstream.chat.android.client.api2.model.response.QueryBannedUsersResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@MoshiApi
@AuthenticatedApi
internal interface ModerationApi {

    @POST("/moderation/mute")
    fun muteUser(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: MuteUserRequest,
    ): RetrofitCall<MuteUserResponse>

    @POST("/moderation/unmute")
    fun unmuteUser(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: MuteUserRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/moderation/mute/channel")
    fun muteChannel(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: MuteChannelRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/moderation/unmute/channel")
    fun unmuteChannel(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: MuteChannelRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/moderation/flag")
    fun flag(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: Map<String, String>,
    ): RetrofitCall<FlagResponse>

    @POST("/moderation/unflag")
    fun unflag(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: Map<String, String>,
    ): RetrofitCall<FlagResponse>

    @POST("/moderation/ban")
    fun banUser(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: BanUserRequest,
    ): RetrofitCall<CompletableResponse>

    @DELETE("/moderation/ban")
    fun unbanUser(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Query("target_user_id") targetUserId: String,
        @Query("type") channelType: String,
        @Query("id") channelId: String,
        @Query("shadow") shadow: Boolean,
    ): RetrofitCall<CompletableResponse>

    @GET("/query_banned_users")
    fun queryBannedUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryBannedUsersRequest,
    ): RetrofitCall<QueryBannedUsersResponse>
}
