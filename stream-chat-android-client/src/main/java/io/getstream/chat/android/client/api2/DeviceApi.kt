package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.model.requests.AddDeviceRequest
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.DevicesResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@AuthenticatedApi
internal interface DeviceApi {

    @GET("/devices")
    fun getDevices(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<DevicesResponse>

    @POST("devices")
    fun addDevices(
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
        @Body request: AddDeviceRequest,
    ): RetrofitCall<CompletableResponse>

    @DELETE("/devices")
    fun deleteDevice(
        @Query("id") deviceId: String,
        @Query(QueryParams.CONNECTION_ID) connectionId: String,
    ): RetrofitCall<CompletableResponse>
}
