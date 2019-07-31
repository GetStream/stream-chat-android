package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.rest.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.PaginationRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;

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

public interface ChannelRetrofit {
    @POST("/channels/{type}/{id}/query")
    Call<ChannelResponse> query(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body ChannelDetailRequest request);

    @DELETE("/channels/{type}/{id}")
    Call<ChannelResponse> deleteChannel(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @POST("/channels/{type}/{id}/query")
    Call<ChannelResponse> creatchatWithInvitation(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body ChannelDetailRequest request);

    @POST("/channels/{type}/{id}/stop-watching")
    Call<ChannelResponse> stopWatching(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, String> body);

    @POST("/channels/{type}/{id}/query")
    Call<ChannelResponse> pagination(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body PaginationRequest request);

    @POST("/channels/{type}/{id}")
    Call<ChannelResponse> acceptInvite(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, Object> body);

    @POST("/channels/{type}/{id}")
    Call<ChannelResponse> rejectInvite(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, Object> body);

    @POST("/channels/{type}/{id}/message")
    Call<MessageResponse> sendMessage(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Body SendMessageRequest request);

    @POST("/messages/{id}/action")
    Call<MessageResponse> sendAction(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendActionRequest request);

    @POST("/messages/{id}/reaction")
    Call<MessageResponse> sendReaction(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body ReactionRequest request);

    @DELETE("/messages/{id}/reaction/{type}")
    Call<MessageResponse> deleteReaction(@Path("id") String messageId, @Path("type") String reactionType, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @GET("/messages/{parent_id}/replies")
    Call<GetRepliesResponse> getReplies(@Path("parent_id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("limit") String limit);

    @POST("/channels/{type}/{id}/event")
    Call<EventResponse> sendEvent(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendEventRequest request);

    @POST("/channels/{type}/{id}/read")
    Call<EventResponse> markRead(@Path("type") String channelType, @Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body MarkReadRequest request);

    @Multipart
    @POST("/channels/{type}/{id}/image")
    Call<FileSendResponse> sendImage(@Path("type") String channelType, @Path("id") String channelId, @Part MultipartBody.Part file, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @Multipart
    @POST("/channels/{type}/{id}/file")
    Call<FileSendResponse> sendFile(@Path("id") String channelId, @Part MultipartBody.Part file, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);
}
