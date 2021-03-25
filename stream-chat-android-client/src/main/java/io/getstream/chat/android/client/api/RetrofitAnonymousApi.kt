package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.GuestUserRequest
import io.getstream.chat.android.client.api.models.TokenResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.POST

@AnonymousApi
internal interface RetrofitAnonymousApi {

    @POST("/guest")
    fun getGuestUser(
        @Body body: GuestUserRequest,
    ): RetrofitCall<TokenResponse>
}
