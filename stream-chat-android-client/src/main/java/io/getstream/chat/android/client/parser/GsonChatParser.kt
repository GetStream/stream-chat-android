package io.getstream.chat.android.client.parser

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class GsonChatParser : ChatParser {

    @VisibleForTesting(otherwise = PRIVATE)
    internal val gson: Gson = StreamGson.gson

    override fun toJson(any: Any): String {
        return gson.toJson(any)
    }

    override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T {
        return gson.fromJson(raw, clazz)
    }

    override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        return builder
            .addConverterFactory(UrlQueryPayloadFactory(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
    }
}
