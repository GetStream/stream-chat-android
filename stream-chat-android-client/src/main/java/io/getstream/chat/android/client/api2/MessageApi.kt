package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.model.requests.MessageRequest
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateMessageRequest
import io.getstream.chat.android.client.api2.model.requests.ReactionRequest
import io.getstream.chat.android.client.api2.model.requests.SendActionRequest
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.api2.model.response.MessagesResponse
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

@AuthenticatedApi
internal interface MessageApi {

    @POST("/channels/{type}/{id}/message")
    fun sendMessage(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body message: MessageRequest,
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{id}")
    fun getMessage(
        @Path("id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}")
    fun updateMessage(
        @Path("id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body message: MessageRequest,
    ): RetrofitCall<MessageResponse>

    @PUT("/messages/{id}")
    fun partialUpdateMessage(
        @Path("id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: PartialUpdateMessageRequest,
    ): RetrofitCall<MessageResponse>

    @DELETE("/messages/{id}")
    fun deleteMessage(
        @Path("id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}/action")
    fun sendAction(
        @Path("id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: SendActionRequest,
    ): RetrofitCall<MessageResponse>

    @POST("/messages/{id}/reaction")
    fun sendReaction(
        @Path("id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: ReactionRequest,
    ): RetrofitCall<ReactionResponse>

    @DELETE("/messages/{id}/reaction/{type}")
    fun deleteReaction(
        @Path("id") messageId: String,
        @Path("type") reactionType: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{id}/reactions")
    fun getReactions(
        @Path("id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): RetrofitCall<ReactionsResponse>

    @POST("/messages/{messageId}/translate")
    fun translate(
        @Path("messageId") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: TranslateMessageRequest,
    ): RetrofitCall<MessageResponse>

    @GET("/messages/{parent_id}/replies")
    fun getReplies(
        @Path("parent_id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Query("limit") limit: Int,
    ): RetrofitCall<MessagesResponse>

    @GET("/messages/{parent_id}/replies")
    fun getRepliesMore(
        @Path("parent_id") messageId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Query("limit") limit: Int,
        @Query("id_lt") firstId: String,
    ): RetrofitCall<MessagesResponse>
}
