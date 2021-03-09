package io.getstream.chat.android.client.api2

import com.squareup.moshi.Moshi
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class MoshiUrlQueryPayloadFactory(private val moshi: Moshi) : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation?>,
        retrofit: Retrofit,
    ): Converter<*, String>? {
        return if (annotations.filterIsInstance<UrlQueryPayload>().isNotEmpty()) {
            UrlQueryPayloadConverted(moshi, type)
        } else {
            super.stringConverter(type, annotations, retrofit)
        }
    }

    private class UrlQueryPayloadConverted(
        private val moshi: Moshi,
        private val type: Type,
    ) : Converter<Any, String> {
        override fun convert(value: Any): String {
            return moshi.adapter<Any>(type).toJson(value)
        }
    }
}
