package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.utils.Result
import okhttp3.Response
import retrofit2.Retrofit

internal interface ChatParser {

    fun toJson(any: Any): String
    fun <T : Any> fromJson(raw: String, clazz: Class<T>): T
    fun <T : Any> fromJsonOrError(raw: String, clazz: Class<T>): Result<T>
    fun toError(okHttpResponse: Response): ChatNetworkError
    fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder
}
