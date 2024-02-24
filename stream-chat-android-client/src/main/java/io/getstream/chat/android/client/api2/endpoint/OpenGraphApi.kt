package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api.QueryParams
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API for open graph data.
 */
@AuthenticatedApi
internal interface OpenGraphApi {

    @GET("/og")
    fun get(@Query(QueryParams.URL) url: String): RetrofitCall<AttachmentDto>

}