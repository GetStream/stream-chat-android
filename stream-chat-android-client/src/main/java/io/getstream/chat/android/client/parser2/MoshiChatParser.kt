package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.parser2.adapters.AttachmentDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamMessageDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamReactionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.DownstreamUserDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamMessageDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamReactionDtoAdapter
import io.getstream.chat.android.client.parser2.adapters.UpstreamUserDtoAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal class MoshiChatParser : ChatParser {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addAdapter(DateAdapter())
            .add(DownstreamMessageDtoAdapter)
            .add(UpstreamMessageDtoAdapter)
            .add(AttachmentDtoAdapter)
            .add(DownstreamReactionDtoAdapter)
            .add(UpstreamReactionDtoAdapter)
            .add(DownstreamUserDtoAdapter)
            .add(UpstreamUserDtoAdapter)
            .build()
    }

    override fun toJson(any: Any): String {
        val adapter = moshi.adapter(any.javaClass)
        return adapter.toJson(any)
    }

    override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T {
        val adapter = moshi.adapter(clazz)
        return adapter.fromJson(raw)!!
    }

    override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.addConverterFactory(MoshiConverterFactory.create(moshi))
    }

    private inline fun <reified T> Moshi.Builder.addAdapter(adapter: JsonAdapter<T>) = apply {
        this.add(T::class.java, adapter)
    }
}
