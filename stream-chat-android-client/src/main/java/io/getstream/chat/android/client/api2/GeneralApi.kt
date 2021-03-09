package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api2.model.requests.QueryMembersRequest
import io.getstream.chat.android.client.api2.model.requests.SearchMessagesRequest
import io.getstream.chat.android.client.api2.model.response.QueryMembersResponse
import io.getstream.chat.android.client.api2.model.response.SearchMessagesResponse
import io.getstream.chat.android.client.call.RetrofitCall
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.OPTIONS
import retrofit2.http.Query

@MoshiApi
@AuthenticatedApi
internal interface GeneralApi {
    @OPTIONS("/connect")
    fun warmUp(): RetrofitCall<ResponseBody>

    @GET("/search")
    fun searchMessages(
        @Query("api_key") apiKey: String,
        @Query("client_id") connectionId: String,
        @UrlQueryPayload @Query("payload") payload: SearchMessagesRequest,
    ): RetrofitCall<SearchMessagesResponse>

    @GET("/members")
    fun queryMembers(
        @Query("api_key") apiKey: String,
        @Query("connection_id") connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryMembersRequest,
    ): RetrofitCall<QueryMembersResponse>
}
