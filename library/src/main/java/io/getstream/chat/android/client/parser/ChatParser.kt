package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.errors.ChatNetworkError
import okhttp3.Response
import retrofit2.Retrofit

interface ChatParser {

    companion object {
        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }



    fun toJson(any: Any): String
    fun <T> fromJson(raw: String, clazz: Class<T>): T
    fun <T> fromJsonOrError(raw: String, clazz: Class<T>): Result<T>
    fun toError(okHttpResponse: Response): ChatNetworkError
    fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder
}