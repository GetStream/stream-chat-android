package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AnonymousApi
import io.getstream.chat.android.client.api2.model.requests.GuestUserRequest
import io.getstream.chat.android.client.api2.model.response.TokenResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.POST

@AnonymousApi
@MoshiApi
internal interface GuestApi {

    @POST("/guest")
    fun getGuestUser(
        @Body body: GuestUserRequest,
    ): RetrofitCall<TokenResponse>
}
