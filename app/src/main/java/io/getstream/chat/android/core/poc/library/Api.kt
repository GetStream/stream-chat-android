package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.requests.ChannelsQuery

class Api(
    val apiService: RetrofitApiService
) {

    private val callMapper = RetrofitCallMapper()

    fun queryChannels(query: ChannelsQuery): Call<List<ChatChannel>> {
        return callMapper.map(apiService.queryChannels(query))
    }

}