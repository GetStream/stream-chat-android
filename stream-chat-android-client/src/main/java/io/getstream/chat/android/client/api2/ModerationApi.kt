package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api2.model.requests.BanUserRequest
import io.getstream.chat.android.client.api2.model.requests.MuteChannelRequest
import io.getstream.chat.android.client.api2.model.requests.MuteUserRequest
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.client.api2.model.response.MuteUserResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Query

@MoshiApi
@AuthenticatedApi
internal interface ModerationApi {

    @POST("/moderation/mute")
    fun muteUser(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body body: MuteUserRequest,
    ): RetrofitCall<MuteUserResponse>

    @POST("/moderation/unmute")
    fun unMuteUser(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body body: MuteUserRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/moderation/mute/channel")
    fun muteChannel(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("connection_id") connectionId: String,
        @Body body: MuteChannelRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/moderation/unmute/channel")
    fun unMuteChannel(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("connection_id") connectionId: String,
        @Body body: MuteChannelRequest,
    ): RetrofitCall<CompletableResponse>

    @POST("/moderation/flag")
    fun flag(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String,
        @Body body: Map<String, String>
    ): RetrofitCall<FlagResponse>

    @POST("/moderation/unflag")
    fun unFlag(
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
    fun unBanUser(
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @Query("target_user_id") targetUserId: String,
        @Query("type") channelType: String,
        @Query("id") channelId: String,
        @Query("shadow") shadow: Boolean,
    ): RetrofitCall<CompletableResponse>
}
