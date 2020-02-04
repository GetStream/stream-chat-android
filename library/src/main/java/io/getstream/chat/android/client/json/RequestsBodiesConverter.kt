package io.getstream.chat.android.client.json

import com.google.gson.Gson
import io.getstream.chat.android.client.QueryChannelsRequest
import io.getstream.chat.android.client.requests.QueryUsers
import io.getstream.chat.android.client.rest.RemoveMembersRequest
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class RequestsBodiesConverter(val gson: Gson) : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation?>,
        retrofit: Retrofit
    ): Converter<*, String>? {

        return if (type === RemoveMembersRequest::class.java) {
            RemoveMembersRequestConverter(gson)
        } else if (type === QueryUsers::class.java) {
            QueryUsersConverter(gson)
        } else if (type === QueryChannelsRequest::class.java) {
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

private class QueryUsersConverter(val gson: Gson) : Converter<QueryUsers, String> {
    override fun convert(value: QueryUsers): String {
        return gson.toJson(value)
    }
}

private class RemoveMembersRequestConverter(val gson: Gson) :
    Converter<RemoveMembersRequest, String> {
    override fun convert(value: RemoveMembersRequest): String {
        return gson.toJson(value)
    }
}