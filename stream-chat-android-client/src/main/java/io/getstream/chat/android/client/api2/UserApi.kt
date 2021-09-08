package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.model.requests.PartialUpdateUsersRequest
import io.getstream.chat.android.client.api2.model.requests.QueryUsersRequest
import io.getstream.chat.android.client.api2.model.requests.UpdateUsersRequest
import io.getstream.chat.android.client.api2.model.response.UpdateUsersResponse
import io.getstream.chat.android.client.api2.model.response.UsersResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface UserApi {
    @POST("/users")
    fun updateUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: UpdateUsersRequest,
    ): RetrofitCall<UpdateUsersResponse>

    @PATCH("/users")
    @JvmSuppressWildcards // See issue: https://github.com/square/retrofit/issues/3275
    fun partialUpdateUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body body: PartialUpdateUsersRequest,
    ): RetrofitCall<UpdateUsersResponse>

    @GET("/users")
    fun queryUsers(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @UrlQueryPayload @Query("payload") payload: QueryUsersRequest,
    ): RetrofitCall<UsersResponse>
}
