package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import io.getstream.chat.android.client.api.models.AddDeviceRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.RemoveMembersRequest
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class RequestsBodiesConverter(val gson: Gson) : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation?>,
        retrofit: Retrofit
    ): Converter<*, String>? {

        return when {
            type === QueryUsersRequest::class.java -> {
                QueryUsersRequestConverter(gson)
            }
            type === AddDeviceRequest::class.java -> {
                AddDeviceRequestConverter(gson)
            }
            type === RemoveMembersRequest::class.java -> {
                RemoveMembersRequestConverter(gson)
            }
            type === QueryUsersRequest::class.java -> {
                QueryUsersConverter(gson)
            }
            type === QueryChannelsRequest::class.java -> {
                QueryChannelsRequestConverter(gson)
            }
            else -> super.stringConverter(type, annotations, retrofit)
        }
    }
}

private class QueryChannelsRequestConverter(val gson: Gson) :
    Converter<QueryChannelsRequest, String> {
    override fun convert(value: QueryChannelsRequest): String {
        return gson.toJson(value)
    }
}

private class QueryUsersConverter(val gson: Gson) : Converter<QueryUsersRequest, String> {
    override fun convert(value: QueryUsersRequest): String {
        return gson.toJson(value)
    }
}

private class RemoveMembersRequestConverter(val gson: Gson) :
    Converter<RemoveMembersRequest, String> {
    override fun convert(value: RemoveMembersRequest): String {
        return gson.toJson(value)
    }
}

private class AddDeviceRequestConverter(val gson: Gson) :
    Converter<AddDeviceRequest, String> {
    override fun convert(value: AddDeviceRequest): String {
        return gson.toJson(value)
    }
}

private class QueryUsersRequestConverter(val gson: Gson) :
    Converter<QueryUsersRequest, String> {
    override fun convert(value: QueryUsersRequest): String {
        return gson.toJson(value)
    }
}