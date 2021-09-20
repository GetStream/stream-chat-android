package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.model.requests.QueryMembersRequest
import io.getstream.chat.android.client.api2.model.requests.SearchMessagesRequest
import io.getstream.chat.android.client.api2.model.requests.SyncHistoryRequest
import io.getstream.chat.android.client.api2.model.response.QueryMembersResponse
import io.getstream.chat.android.client.api2.model.response.SearchMessagesResponse
import io.getstream.chat.android.client.api2.model.response.SyncHistoryResponse
import io.getstream.chat.android.client.call.RetrofitCallBlah
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.OPTIONS
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface GeneralApi {
    @OPTIONS("/connect")
    fun warmUp(): RetrofitCallBlah<ResponseBody>

    @POST("/sync")
    fun getSyncHistory(
        @Body body: SyncHistoryRequest,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCallBlah<SyncHistoryResponse>

    @GET("/search")
    fun searchMessages(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @UrlQueryPayload @Query("payload") payload: SearchMessagesRequest,
    ): RetrofitCallBlah<SearchMessagesResponse>

    @GET("/members")
    fun queryMembers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryMembersRequest,
    ): RetrofitCallBlah<QueryMembersResponse>
}
