package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse

class Api(private val retrofitApi: RetrofitApi) {

    var apiKey: String = ""
    var userId: String = ""
    var clientId: String = ""

    private val callMapper = RetrofitCallMapper()

    fun queryChannels(query: QueryChannelsRequest): Call<QueryChannelsResponse> {
        return callMapper.map(
            retrofitApi.queryChannels(
                apiKey,
                userId,
                clientId,
                query
            )
        )
    }

}