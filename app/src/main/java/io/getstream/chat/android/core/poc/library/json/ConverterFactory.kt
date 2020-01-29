package io.getstream.chat.android.core.poc.library.json

import com.google.gson.Gson
import io.getstream.chat.android.core.poc.library.QueryChannelsRequest
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class ConverterFactory(val gson: Gson) : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation?>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return if (type === QueryChannelsRequest::class.java) {
            QueryChannelsRequestConverter(gson)
        } else super.stringConverter(type, annotations, retrofit)
    }
}

private class QueryChannelsRequestConverter(val gson: Gson) :
    Converter<QueryChannelsRequest, String> {
    override fun convert(value: QueryChannelsRequest): String {
        return gson.toJson(value)
    }
}