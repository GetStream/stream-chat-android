package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.GuestUserRequest
import io.getstream.chat.android.client.api.models.TokenResponse
import io.getstream.chat.android.client.call.RetrofitCall
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.OPTIONS
import retrofit2.http.POST
import retrofit2.http.Query

internal interface RetrofitAnonymousApi {

    @POST("/guest")
    fun getGuestUser(
        @Query("api_key") apiKey: String,
        @Body body: GuestUserRequest
    ): RetrofitCall<TokenResponse>

    @OPTIONS("/connect")
    fun warmUp(): RetrofitCall<ResponseBody>
}
