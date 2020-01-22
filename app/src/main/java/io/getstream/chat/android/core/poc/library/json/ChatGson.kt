package io.getstream.chat.android.core.poc.library.json

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object ChatGson {
    val instance: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapterFactory(TypeAdapterFactory())
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .setPrettyPrinting()
            .addSerializationExclusionStrategy(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>): Boolean {
                    return false
                }

                override fun shouldSkipField(f: FieldAttributes): Boolean {
                    return f.getAnnotation(IgnoreSerialisation::class.java) != null
                }

            })
            .create()
    }
}