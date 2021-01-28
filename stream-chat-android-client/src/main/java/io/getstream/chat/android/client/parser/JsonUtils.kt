package io.getstream.chat.android.client.parser

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public object JsonUtils {

    public fun defaultStreamGson() : Gson {
        return GsonBuilder()
            .registerTypeAdapterFactory(TypeAdapterFactory())
            .addSerializationExclusionStrategy(
                object : ExclusionStrategy {
                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        return false
                    }

                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        return f.getAnnotation(IgnoreSerialisation::class.java) != null
                    }
                }
            )
            .addDeserializationExclusionStrategy(
                object : ExclusionStrategy {
                    override fun shouldSkipClass(clazz: Class<*>): Boolean {
                        return false
                    }

                    override fun shouldSkipField(f: FieldAttributes): Boolean {
                        return f.getAnnotation(IgnoreDeserialisation::class.java) != null
                    }
                }
            )
            .create()
    }
}
