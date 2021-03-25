package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.UploadFileResponse
import io.getstream.chat.android.client.call.RetrofitCall
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

@AuthenticatedApi
internal interface RetrofitCdnApi {
    @Multipart
    @POST("/channels/{type}/{id}/image")
    fun sendImage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Part file: MultipartBody.Part,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<UploadFileResponse>

    @Multipart
    @POST("/channels/{type}/{id}/file")
    fun sendFile(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Part file: MultipartBody.Part,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<UploadFileResponse>

    @DELETE("/channels/{type}/{id}/file")
    fun deleteFile(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Query("url") url: String,
    ): RetrofitCall<CompletableResponse>

    @DELETE("/channels/{type}/{id}/image")
    fun deleteImage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Query("url") url: String,
    ): RetrofitCall<CompletableResponse>
}
