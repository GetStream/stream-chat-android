package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.parser.ChatParser
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class MoshiChatParser : ChatParser {

    private val logger = ChatLogger.get("MoshiChatParser")

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addAdapter(DateAdapter())
            .build()
    }

    override fun toJson(any: Any): String {
        return moshi.adapter(any.javaClass).toJson(any)
    }

    override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T {
        return moshi.adapter(clazz).fromJson(raw)!!
    }

    override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.addConverterFactory(MoshiConverterFactory.create(moshi))
    }

    private inline fun <reified T> Moshi.Builder.addAdapter(adapter: JsonAdapter<T>) = apply {
        this.add(T::class.java, adapter)
    }
}
