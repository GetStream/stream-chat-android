package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.GuestUserRequest
import io.getstream.chat.android.client.api.models.TokenResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

@AnonymousApi
internal interface RetrofitAnonymousApi {

    @POST("/guest")
    fun getGuestUser(
        @Query(QueryParams.API_KEY) apiKey: String,
        @Body body: GuestUserRequest
    ): RetrofitCall<TokenResponse>
}
