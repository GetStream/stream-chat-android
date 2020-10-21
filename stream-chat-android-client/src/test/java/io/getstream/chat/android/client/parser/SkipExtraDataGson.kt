package io.getstream.chat.android.client.parser

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal class SkipExtraDataGson {

    private val extraDataName = "extraData"

    val instance: Gson by lazy {
        GsonBuilder()
            .addSerializationExclusionStrategy(
                object : ExclusionStrategy {
                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        return false
                    }

                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        val name = f.name
                        return name == extraDataName
                    }
                }
            )
            .addDeserializationExclusionStrategy(
                object : ExclusionStrategy {
                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        return false
                    }

                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        val name = f.name
                        return name == extraDataName
                    }
                }
            )
            .create()
    }

    fun toJson(obj: Any): String {
        return instance.toJson(obj)
    }

    fun <T> fromJson(obj: String, type: Class<T>): T {
        return instance.fromJson(obj, type)
    }
}
