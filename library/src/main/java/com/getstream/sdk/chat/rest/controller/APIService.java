package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.rest.apimodel.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.apimodel.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.apimodel.request.PaginationRequest;
import com.getstream.sdk.chat.rest.apimodel.request.ReactionRequest;
import com.getstream.sdk.chat.rest.apimodel.request.SendActionRequest;
import com.getstream.sdk.chat.rest.apimodel.request.SendEventRequest;
import com.getstream.sdk.chat.rest.apimodel.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.EventResponse;
import com.getstream.sdk.chat.rest.apimodel.response.FileSendResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetChannelsResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.MessageResponse;

import org.json.JSONObject;

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
    @GET("/channels")
    Call<GetChannelsResponse> getChannels(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("payload") JSONObject payload);

    @POST("/channels/messaging/{id}/query")
    Call<ChannelResponse> chatDetail(@Path("id") String channlId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body ChannelDetailRequest request);

    @POST("/channels/messaging/{id}/query")
    Call<ChannelResponse> pagination(@Path("id") String channlId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body PaginationRequest request);

    @POST("/channels/messaging/{id}/message")
    Call<MessageResponse> sendMessage(@Path("id") String channlId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendMessageRequest request);

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
    Call<GetRepliesResponse> getReplies(@Path("parent_id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @POST("/channels/messaging/{id}/event")
    Call<EventResponse> sendEvent(@Path("id") String channlId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body SendEventRequest request);

    @POST("/channels/messaging/{id}/read")
    Call<EventResponse> readMark(@Path("id") String channlId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body MarkReadRequest request);

    @Multipart
    @POST("/channels/messaging/{id}/image")
    Call<FileSendResponse> sendImage(@Path("id") String channlId, @Part MultipartBody.Part file, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @Multipart
    @POST("/channels/messaging/{id}/file")
    Call<FileSendResponse> sendFile(@Path("id") String channlId, @Part MultipartBody.Part file, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);
}
