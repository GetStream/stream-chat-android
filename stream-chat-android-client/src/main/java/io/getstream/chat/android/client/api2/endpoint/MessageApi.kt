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

package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateMessageRequest
import io.getstream.chat.android.client.api2.model.requests.ReactionRequest
import io.getstream.chat.android.client.api2.model.requests.SendActionRequest
import io.getstream.chat.android.client.api2.model.requests.SendMessageRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateMessageRequest
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.api2.model.response.MessagesResponse
import io.getstream.chat.android.client.api2.model.response.QueryDraftMessagesResponse
import io.getstream.chat.android.client.api2.model.response.ReactionResponse
import io.getstream.chat.android.client.api2.model.response.ReactionsResponse
import io.getstream.chat.android.client.api2.model.response.TranslateMessageRequest
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("TooManyFunctions")
@AuthenticatedApi
internal interface MessageApi {

    /**
     * [REST documentation](https://getstream.io/chat/docs/rest/#messages-sendmessage)
     */
    @POST("/channels/{type}/{id}/message")
    fun sendMessage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body message: SendMessageRequest,
    ): RetrofitCall<MessageResponse>


    @POST("/channels/{type}/{id}/draft")
    fun createDraftMessage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Body message: SendMessageRequest,
    ): RetrofitCall<MessageResponse>

    @DELETE("/channels/{type}/{id}/draft")
    fun deleteDraftMessage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
    ): RetrofitCall<MessageResponse>

    @POST("/drafts/query")
    fun queryDraftMessages(): RetrofitCall<QueryDraftMessagesResponse>

    @GET("/messages/{id}")
    fun getMessage(@Path("id") messageId: String): RetrofitCall<MessageResponse>

    /**
     * [REST documentation]()https://getstream.io/chat/docs/rest/#messages-updatemessage)
     */
    @POST("/messages/{id}")
    fun updateMessage(
        @Path("id") messageId: String,
        @Body message: UpdateMessageRequest,
    ): RetrofitCall<MessageResponse>

    /**
     * [Rest documentation](https://getstream.io/chat/docs/rest/#messages-updatemessagepartial-request)
     */
    @PUT("/messages/{id}")
    fun partialUpdateMessage(
        @Path("id") messageId: String,
        @Body body: PartialUpdateMessageRequest,
    ): RetrofitCall<MessageResponse>

    @DELETE("/messages/{id}")
    fun deleteMessage(
        @Path("id") messageId: String,
        @Query(QueryParams.HARD_DELETE) hard: Boolean?,
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}/action")
    fun sendAction(
        @Path("id") messageId: String,
        @Body request: SendActionRequest,
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}/reaction")
    fun sendReaction(
        @Path("id") messageId: String,
        @Body request: ReactionRequest,
    ): RetrofitCall<ReactionResponse>

    @DELETE("/messages/{id}/reaction/{type}")
    fun deleteReaction(
        @Path("id") messageId: String,
        @Path("type") reactionType: String,
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{id}/reactions")
    fun getReactions(
        @Path("id") messageId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): RetrofitCall<ReactionsResponse>

    @POST("/messages/{messageId}/translate")
    fun translate(
        @Path("messageId") messageId: String,
        @Body request: TranslateMessageRequest,
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{parent_id}/replies")
    fun getReplies(
        @Path("parent_id") messageId: String,
        @Query("limit") limit: Int,
    ): RetrofitCall<MessagesResponse>

    @GET("/messages/{parent_id}/replies?sort=[{\"field\":\"created_at\",\"direction\":1}]")
    fun getNewerReplies(
        @Path("parent_id") parentId: String,
        @Query("limit") limit: Int,
        @Query("id_gt") lastId: String?,
    ): RetrofitCall<MessagesResponse>

    @GET("/messages/{parent_id}/replies")
    fun getRepliesMore(
        @Path("parent_id") messageId: String,
        @Query("limit") limit: Int,
        @Query("id_lt") firstId: String,
    ): RetrofitCall<MessagesResponse>
}
