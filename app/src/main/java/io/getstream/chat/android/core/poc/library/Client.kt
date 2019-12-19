package io.getstream.chat.android.core.poc.library

class Client {

    private val api = Api(RetrofitApiBuilder().build())

    fun queryChannels(): Call<List<Channel>> {
        return api.queryChannels()
    }

}