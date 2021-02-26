package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api2.model.requests.UpdateCooldownRequest
import io.getstream.chat.android.client.api2.model.response.ChannelResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@MoshiApi
@AuthenticatedApi
internal interface ChannelApi {

    @POST("/channels/{type}/{id}")
    fun updateCooldown(
        @Path("type") channelType: String,
        @Path("id") channelId: String,
        @Query("api_key") apiKey: String,
        @Query("client_id") clientID: String,
        @Body body: UpdateCooldownRequest,
    ): RetrofitCall<ChannelResponse>
}
