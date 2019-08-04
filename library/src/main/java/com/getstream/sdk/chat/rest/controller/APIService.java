package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.rest.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.request.QueryChannelRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.PaginationRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.AddDevicesResponse;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.rest.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.QueryUserListResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;

import org.json.JSONObject;

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
    Call<QueryChannelsResponse> queryChannels(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("payload") JSONObject payload);

    @POST("/channels/messaging/{id}/query")
    Call<ChannelResponse> queryChannel(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body QueryChannelRequest request);

    @DELETE("/channels/messaging/{id}")
    Call<ChannelResponse> deleteChannel(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @POST("/channels/messaging/{id}/query")
    Call<ChannelResponse> creatchatWithInvitation(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body QueryChannelRequest request);

    @POST("/channels/messaging/{id}/stop-watching")
    Call<ChannelResponse> chatStopWatch(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, String> body);

    @POST("/channels/messaging/{id}/query")
    Call<ChannelResponse> pagination(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body PaginationRequest request);

    @POST("/channels/messaging/{id}")
    Call<ChannelResponse> acceptInvite(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, Object> body);

    @POST("/channels/messaging/{id}")
    Call<ChannelResponse> rejectInvite(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body Map<String, Object> body);
    // endregion

    // region User
    @GET("/users")
    Call<QueryUserListResponse> queryUsers(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("payload") JSONObject payload);

    @POST("/channels/messaging/{id}")
    Call<ChannelResponse> addMembers(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body JSONObject body);
    // endregion

    // region Message
    @POST("/channels/messaging/{id}/message")
    Call<MessageResponse> sendMessage(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendMessageRequest request);

    @POST("/messages/{id}")
    Call<MessageResponse> updateMessage(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body UpdateMessageRequest request);

    @POST("/messages/{id}/action")
    Call<MessageResponse> sendAction(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendActionRequest request);

    @DELETE("/messages/{id}")
    Call<MessageResponse> deleteMessage(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @POST("/messages/{id}/reaction")
    Call<MessageResponse> sendReaction(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body ReactionRequest request);

    @DELETE("/messages/{id}/reaction/{type}")
    Call<MessageResponse> deleteReaction(@Path("id") String messageId, @Path("type") String reactionType, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @GET("/messages/{parent_id}/replies")
    Call<GetRepliesResponse> getReplies(@Path("parent_id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("limit") String limit);

    @GET("/messages/{parent_id}/replies")
    Call<GetRepliesResponse> getRepliesMore(@Path("parent_id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("limit") String limit, @Query("id_lt") String firstId);

    @POST("/channels/messaging/{id}/event")
    Call<EventResponse> sendEvent(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendEventRequest request);

    @POST("/channels/messaging/{id}/read")
    Call<EventResponse> readMark(@Path("id") String channelId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body MarkReadRequest request);

    @Multipart
    @POST("/channels/messaging/{id}/image")
    Call<FileSendResponse> sendImage(@Path("id") String channelId, @Part MultipartBody.Part file, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @Multipart
    @POST("/channels/messaging/{id}/file")
    Call<FileSendResponse> sendFile(@Path("id") String channelId, @Part MultipartBody.Part file, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);
    // endregion

    // region Device
    @GET("/devices")
    Call<GetDevicesResponse> getDevices(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("userID") Map body);

    @POST("devices")
    Call<AddDevicesResponse> addDevices(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body AddDeviceRequest request);

    // endregion
}
