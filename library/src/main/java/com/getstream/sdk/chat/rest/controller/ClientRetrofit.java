package com.getstream.sdk.chat.rest.controller;

import com.getstream.sdk.chat.rest.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.AddDevicesResponse;
import com.getstream.sdk.chat.rest.response.GetChannelsResponse;
import com.getstream.sdk.chat.rest.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.response.GetUsersResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;

import org.json.JSONObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ClientRetrofit {
    @GET("/channels")
    Call<GetChannelsResponse> queryChannels(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("payload") JSONObject payload);

    @GET("/users")
    Call<GetUsersResponse> queryUsers(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("payload") JSONObject payload);

    @POST("/messages/{id}")
    Call<MessageResponse> updateMessage(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body UpdateMessageRequest request);

    @DELETE("/messages/{id}")
    Call<MessageResponse> deleteMessage(@Path("id") String messageId, @Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId);

    @GET("/devices")
    Call<GetDevicesResponse> getDevices(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Query("userID") Map body);

    @POST("devices")
    Call<AddDevicesResponse> addDevices(@Query("api_key") String apiKey, @Query("user_id") String userId, @Query("client_id") String connectionId, @Body AddDeviceRequest request);
}
