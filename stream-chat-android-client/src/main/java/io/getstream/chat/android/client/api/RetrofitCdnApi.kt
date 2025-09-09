/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.UploadFileResponse
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.utils.ProgressCallback
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

@AuthenticatedApi
internal interface RetrofitCdnApi {
    @Multipart
    @POST("/channels/{type}/{id}/image")
    fun sendImage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Part file: MultipartBody.Part,
        @Tag progressCallback: ProgressCallback?,
    ): RetrofitCall<UploadFileResponse>

    @Multipart
    @POST("/channels/{type}/{id}/file")
    fun sendFile(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Part file: MultipartBody.Part,
        @Tag progressCallback: ProgressCallback?,
    ): RetrofitCall<UploadFileResponse>

    @DELETE("/channels/{type}/{id}/file")
    fun deleteFile(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("url") url: String,
    ): RetrofitCall<CompletableResponse>

    @DELETE("/channels/{type}/{id}/image")
    fun deleteImage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("url") url: String,
    ): RetrofitCall<CompletableResponse>

    @Multipart
    @POST("/uploads/file")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Tag progressCallback: ProgressCallback?,
    ): RetrofitCall<UploadFileResponse>

    @DELETE("/uploads/file")
    fun deleteFile(
        @Query("url") url: String,
    ): RetrofitCall<CompletableResponse>

    @Multipart
    @POST("/uploads/image")
    fun uploadImage(
        @Part file: MultipartBody.Part,
        @Tag progressCallback: ProgressCallback?,
    ): RetrofitCall<UploadFileResponse>

    @DELETE("/uploads/image")
    fun deleteImage(
        @Query("url") url: String,
    ): RetrofitCall<CompletableResponse>
}
