package io.getstream.chat.android.core.poc.library.json

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.core.poc.library.QueryChannelsRequest
import io.getstream.chat.android.core.poc.library.api.RetrofitClient

class TypeAdapterFactory : com.google.gson.TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType == QueryChannelsRequest::class) {
            return QueryChannelsAdapter(
                gson,
                type
            ) as TypeAdapter<T>
        } else {
            //TODO: replace with chat error
            //throw RuntimeException("undefined type: " + type.rawType)
            return null
        }
    }
}