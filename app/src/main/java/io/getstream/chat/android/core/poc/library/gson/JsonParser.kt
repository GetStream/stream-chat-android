package io.getstream.chat.android.core.poc.library.gson

import io.getstream.chat.android.core.poc.library.Result
import io.getstream.chat.android.core.poc.library.errors.ChatHttpError
import okhttp3.Response
import retrofit2.Retrofit

interface JsonParser {
    fun toJson(any: Any): String
    fun <T> fromJson(raw: String, clazz: Class<T>): T
    fun <T> fromJsonOrError(raw: String, clazz: Class<T>): Result<T>
    fun toError(okHttpResponse: Response): ChatHttpError
    fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder
}