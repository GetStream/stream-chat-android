package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser.ChatParser

internal class RetrofitCallMapper(private val chatParser: ChatParser) {

    fun <T> map(call: retrofit2.Call<T>): Call<T> {
        return RetrofitCall(call, chatParser)
    }
}
