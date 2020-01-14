package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitApi {
    @GET("/channels")
    fun queryChannels(
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") clientID: String,
        @Query("payload") payload: QueryChannelsRequest
    ): Call<QueryChannelsResponse>

    @Multipart
    @POST("/channels/{type}/{id}/image")
    fun sendImage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Part file: MultipartBody.Part,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): Call<UploadFileResponse>

    @Multipart
    @POST("/channels/{type}/{id}/file")
    fun sendFile(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Part file: MultipartBody.Part,
        @Query("api_key") apiKey: String,
        @Query("user_id") userId: String,
        @Query("client_id") connectionId: String
    ): Call<UploadFileResponse>
}
