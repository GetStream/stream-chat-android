package io.getstream.chat.android.client.parser

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class GsonChatParser : ChatParser {

    @VisibleForTesting(otherwise = PRIVATE)
    val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapterFactory(TypeAdapterFactory())
            .addSerializationExclusionStrategy(
                object : ExclusionStrategy {
                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        return false
                    }

                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        return f.getAnnotation(IgnoreSerialisation::class.java) != null
                    }
                }
            )
            .addDeserializationExclusionStrategy(
                object : ExclusionStrategy {
                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        return false
                    }

                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        return f.getAnnotation(IgnoreDeserialisation::class.java) != null
                    }
                }
            )
            .create()
    }

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
