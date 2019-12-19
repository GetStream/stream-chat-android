package io.getstream.chat.android.core.poc.library

class Api(
    val apiService: RetrofitApiService
) {

    private val callMapper = RetrofitCallMapper()

    fun queryChannels(): Call<List<Channel>> {
        return callMapper.map(apiService.queryChannels())
    }

}