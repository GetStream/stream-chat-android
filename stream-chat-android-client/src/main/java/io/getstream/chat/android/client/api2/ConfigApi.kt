package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api2.model.response.AppSettingsAPIResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.GET

/**
 * API for configurations, settings in the dashboard with read and write possibilities (not mandatorily)
 */
@AuthenticatedApi
internal interface ConfigApi {

    @GET("/app")
    fun getAppSettings(): RetrofitCall<AppSettingsAPIResponse>
}
