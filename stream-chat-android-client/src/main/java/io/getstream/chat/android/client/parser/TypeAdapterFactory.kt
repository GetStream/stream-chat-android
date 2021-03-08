package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.client.parser.adapters.CustomObjectGsonAdapter
import io.getstream.chat.android.client.parser.adapters.QuerySortAdapter
import java.util.Date

internal class TypeAdapterFactory : com.google.gson.TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        if (type.rawType.interfaces.contains(CustomObject::class.java)) {
            return CustomObjectGsonAdapter(gson, type.rawType) as TypeAdapter<T>
        }

        if (type.rawType.getAllInterfaces().contains(Map::class.java)) {
            return MapAdapter(gson.getDelegateAdapter(this, type) as TypeAdapter<Map<*, *>>) as TypeAdapter<T>
        }

        return when (type.rawType) {
            ChatEvent::class.java -> {
                EventAdapter(
                    gson,
                    gson.getDelegateAdapter(this, type) as TypeAdapter<ChatEvent>
                ) as TypeAdapter<T>
            }
            Date::class.java -> {
                DateAdapter() as TypeAdapter<T>
            }
            QuerySortAdapter::class.java -> {
                QuerySortAdapter(gson) as TypeAdapter<T>
            }
            FilterObject::class.java -> {
                FilterObjectTypeAdapter(gson) as TypeAdapter<T>
            }
            else -> {
                null
            }
        }
    }
}

private fun Class<*>.getAllInterfaces(): List<Class<*>> = if (this == Object::class.java) {
    emptyList()
} else {
    interfaces.toList() + (superclass?.getAllInterfaces() ?: emptyList())
}
