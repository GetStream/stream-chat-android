package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class UrlQueryPayloadFactory(private val gson: Gson) : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation?>,
        retrofit: Retrofit
    ): Converter<*, String>? {

        return if (annotations.filterIsInstance<UrlQueryPayload>().isNotEmpty()) {
            UrlQueryPayloadConverted(gson)
        } else {
            super.stringConverter(type, annotations, retrofit)
        }
    }
}

private class UrlQueryPayloadConverted(val gson: Gson) : Converter<Any, String> {
    override fun convert(value: Any): String {
        return gson.toJson(value)
    }
}
