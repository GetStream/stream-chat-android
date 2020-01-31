package io.getstream.chat.android.core.poc.library.json

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.core.poc.library.FilterObject
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.api.UserGsonAdapter
import io.getstream.chat.android.core.poc.library.json.adapters.QuerySortAdapter

class TypeAdapterFactory : com.google.gson.TypeAdapterFactory {

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        return when (type.rawType) {
            FilterObject::class -> {
                FilterObjectAdapter(gson) as TypeAdapter<T>
            }
            QuerySortAdapter::class -> {
                QuerySortAdapter(gson) as TypeAdapter<T>
            }
            User::class -> {
                UserGsonAdapter(gson) as TypeAdapter<T>
            }
            else -> {
                null
            }
        }
    }
}