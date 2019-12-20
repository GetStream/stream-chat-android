package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.rest.request.AcceptInviteRequest;
import com.getstream.sdk.chat.rest.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.request.AddMembersRequest;
import com.getstream.sdk.chat.rest.request.BanUserRequest;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.GuestUserRequest;
import com.getstream.sdk.chat.rest.request.HideChannelRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.RejectInviteRequest;
import com.getstream.sdk.chat.rest.request.RemoveMembersRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.UpdateChannelRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FlagResponse;
import com.getstream.sdk.chat.rest.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.response.GetReactionsResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.rest.response.MuteUserResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.rest.response.QueryUserListResponse;
import com.getstream.sdk.chat.rest.response.SearchMessagesResponse;
import com.getstream.sdk.chat.rest.response.TokenResponse;
import com.getstream.sdk.chat.rest.response.UploadFileResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {
    // region Channel
    @GET("/channels")
    Call<QueryChannelsResponse> queryChannels(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String clientID, @Query("payload") String payload);

    @POST("/channels/{type}/{id}/query")
    Call<ChannelState> queryChannel(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String clientID, @Body ChannelQueryRequest request);

    @POST("/channels/{type}/query")
    Call<ChannelState> queryChannel(@Path("type") String channelType, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String clientID, @Body ChannelQueryRequest request);

    @POST("/channels/{type}/{id}")
    Call<ChannelResponse> updateChannel(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String clientID, @Body UpdateChannelRequest body);

    @DELETE("/channels/{type}/{id}")
    Call<ChannelResponse> deleteChannel(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String clientID);

    @POST("/channels/{type}/{id}/stop-watching")
    Call<CompletableResponse> stopWatching(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String clientID, @Body Map body);

    @POST("/channels/{type}/{id}")
    Call<ChannelResponse> acceptInvite(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String clientID, @Body AcceptInviteRequest body);

    @POST("/channels/{type}/{id}")
    Call<ChannelResponse> rejectInvite(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String clientID, @Body RejectInviteRequest body);

    @POST("/channels/{type}/{id}/show")
    Call<CompletableResponse> showChannel(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String clientID, @Body Map body);

    @POST("/channels/{type}/{id}/hide")
    Call<CompletableResponse> hideChannel(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String clientID, @Body HideChannelRequest body);
    // endregion

    // region User
    @POST("/guest")
    Call<TokenResponse> setGuestUser(@Query("api_key") String apiKey, @Body GuestUserRequest body);

    @GET("/users")
    Call<QueryUserListResponse> queryUsers(@Query("api_key") String apiKey, @Query("client_id") String connectionId, @Query("payload") String payload);

    @POST("/channels/{type}/{id}")
    Call<ChannelResponse> addMembers(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String connectionId, @Body AddMembersRequest body);

    @POST("/channels/{type}/{id}")
    Call<ChannelResponse> removeMembers(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String connectionId, @Body RemoveMembersRequest body);

    @POST("/moderation/mute")
    Call<MuteUserResponse> muteUser(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, String> body);

    @POST("/moderation/unmute")
    Call<MuteUserResponse> unMuteUser(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, String> body);

    @POST("/moderation/flag")
    Call<FlagResponse> flag(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, String> body);

    @POST("/moderation/unflag")
    Call<FlagResponse> unFlag(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, String> body);

    @POST("/moderation/ban")
    Call<CompletableResponse> banUser(@Query("api_key") String apiKey, @Query("client_id") String connectionId, @Body BanUserRequest body);

    @DELETE("/moderation/ban")
    Call<CompletableResponse> unBanUser(@Query("api_key") String apiKey, @Query("client_id") String connectionId, @Query("target_user_id") String targetUserId, @Query("type") String channelType, @Query("id") String channelId);
    // endregion

    // region Message
    @POST("/channels/{type}/{id}/message")
    Call<MessageResponse> sendMessage(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map message);

    @POST("/messages/{id}")
    Call<MessageResponse> updateMessage(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map message);

    @GET("/messages/{id}")
    Call<MessageResponse> getMessage(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @POST("/messages/{id}/action")
    Call<MessageResponse> sendAction(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendActionRequest request);

    @DELETE("/messages/{id}")
    Call<MessageResponse> deleteMessage(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @POST("/messages/{id}/reaction")
    Call<MessageResponse> sendReaction(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body ReactionRequest request);

    @DELETE("/messages/{id}/reaction/{type}")
    Call<MessageResponse> deleteReaction(@Path("id") String messageId, @Path("type") String reactionType, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @GET("/messages/{id}/reactions")
    Call<GetReactionsResponse> getReactions(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("client_id") String connectionId, @Query("limit") Integer limit, @Query("offset") Integer offset);

    @GET("/messages/{parent_id}/replies")
    Call<GetRepliesResponse> getReplies(@Path("parent_id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("limit") int limit);

    @GET("/messages/{parent_id}/replies")
    Call<GetRepliesResponse> getRepliesMore(@Path("parent_id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("limit") int limit, @Query("id_lt") String firstId);

    @POST("/channels/{type}/{id}/event")
    Call<EventResponse> sendEvent(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendEventRequest request);

    @POST("/channels/{type}/{id}/read")
    Call<EventResponse> markRead(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body MarkReadRequest request);

    @POST("/channels/read")
    Call<EventResponse> markAllRead(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @Multipart
    @POST("/channels/{type}/{id}/image")
    Call<UploadFileResponse> sendImage(@Path("type") String channelType, @Path("id") String channelId, @Part MultipartBody.Part file, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @Multipart
    @POST("/channels/{type}/{id}/file")
    Call<UploadFileResponse> sendFile(@Path("type") String channelType, @Path("id") String channelId, @Part MultipartBody.Part file, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @DELETE("/channels/{type}/{id}/file")
    Call<CompletableResponse> deleteFile(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String connectionId, @Query("url") String url);

    @DELETE("/channels/{type}/{id}/image")
    Call<CompletableResponse> deleteImage(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("client_id") String connectionId, @Query("url") String url);

    @GET("/search")
    Call<SearchMessagesResponse> searchMessages(@Query("api_key") String apiKey, @Query("client_id") String connectionId, @Query("payload") String payload);

    // endregion

    // region Device
    @GET("/devices")
    Call<GetDevicesResponse> getDevices(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @POST("devices")
    Call<CompletableResponse> addDevices(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body AddDeviceRequest request);

    @DELETE("/devices")
    Call<CompletableResponse> deleteDevice(@Query("id") String deviceId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);
    // endregion
}
