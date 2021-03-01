package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.call.RetrofitCall
import okhttp3.ResponseBody
import retrofit2.http.OPTIONS

@MoshiApi
@AuthenticatedApi
internal interface GeneralApi {
    @OPTIONS("/connect")
    fun warmUp(): RetrofitCall<ResponseBody>
}
