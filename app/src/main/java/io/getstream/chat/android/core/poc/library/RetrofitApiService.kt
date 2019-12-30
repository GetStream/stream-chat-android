package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.requests.ChannelsQuery
import retrofit2.http.*

interface RetrofitApiService {
    @GET("/channels")
    fun queryChannels(query: ChannelsQuery): retrofit2.Call<List<ChatChannel>>
}
