package io.getstream.chat.android.core.poc.library

import retrofit2.http.*

interface RetrofitApiService {
    @GET("/channels")
    fun queryChannels(): retrofit2.Call<List<Channel>>
}
