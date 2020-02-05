package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.api.UserGsonAdapter
import io.getstream.chat.android.client.parser.adapters.QuerySortAdapter

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