package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.parser.adapters.CustomObjectGsonAdapter
import io.getstream.chat.android.client.parser.adapters.QuerySortAdapter
import io.getstream.chat.android.client.utils.FilterObject

class TypeAdapterFactory : com.google.gson.TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        println(type.toString())

        if (type.rawType.interfaces.contains(CustomObject::class.java)) {
            //return CustomObjectGsonAdapter(gson, type.rawType) as TypeAdapter<T>
        }

        return when (type.rawType) {

            FilterObject::class.java -> {
                FilterObjectAdapter(gson) as TypeAdapter<T>
            }
            QuerySortAdapter::class.java -> {
                QuerySortAdapter(gson) as TypeAdapter<T>
            }
            Message::class.java -> {
                CustomObjectGsonAdapter<Message>(gson, type.rawType) as TypeAdapter<T>
            }
            Reaction::class.java -> {
                CustomObjectGsonAdapter<Reaction>(gson, type.rawType) as TypeAdapter<T>
            }
//            Channel::class.java -> {
//                CustomObjectGsonAdapter<Channel>(gson, type.rawType) as TypeAdapter<T>
//            }
//            User::class.java -> {
//                CustomObjectGsonAdapter<User>(gson, type.rawType) as TypeAdapter<T>
//            }

//            Attachment::class.java -> {
//                CustomObjectGsonAdapter<Attachment>(gson, type.rawType) as TypeAdapter<T>
//            }
            else -> {
                null
            }
        }
    }
}