package io.getstream.chat.android.client.parser.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.errors.ChatParsingError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal class CustomObjectGsonAdapter(
    private val gson: Gson,
    private val clazz: Class<*>
) : TypeAdapter<CustomObject>() {

    companion object {
        val logger = ChatLogger.get("CustomObjectAdapter")
    }

    override fun write(writer: JsonWriter, obj: CustomObject?) {

        try {
            if (obj == null) {
                gson.getAdapter(HashMap::class.java).write(writer, null)
            } else {
                val result = HashMap<String, Any?>()
                result += obj.extraData

                for (field in clazz.declaredFields) {
                    if (Modifier.isStatic(field.modifiers)) continue
                    if (field.isSynthetic) continue
                    if (field.getAnnotation(IgnoreSerialisation::class.java) != null) continue

                    field.isAccessible = true

                    var name = field.name
                    val serializedName = field.getAnnotation(SerializedName::class.java)
                    if (serializedName != null) name = serializedName.value

                    val value = field.get(obj)
                    try {
                        result[name] = value
                    } catch (e: Exception) {
                        throw ChatParsingError("unable to set field $name with value $value")
                    }
                }

                gson.getAdapter(HashMap::class.java).write(writer, result)
            }
        } catch (e: Throwable) {
            throw ChatParsingError("custom object serialisation error of $clazz", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun read(reader: JsonReader): CustomObject? {

        try {
            val result = clazz.newInstance() as CustomObject
            val read = gson.getAdapter(HashMap::class.java).read(reader)

            if (read == null) {
                logger.logE("exception case when api returned null where it shouldn't: $reader")
                return null
            }

            val map = read as HashMap<String, Any>
            for (field in clazz.declaredFields) {
                if (Modifier.isStatic(field.modifiers)) continue
                if (field.isSynthetic) continue
                if (field.getAnnotation(IgnoreDeserialisation::class.java) != null) continue

                var name = field.name
                val serializedName = field.getAnnotation(SerializedName::class.java)
                if (serializedName != null) name = serializedName.value

                if (map.containsKey(name)) {
                    field.isAccessible = true
                    val rawValue = gson.toJson(map.remove(name))
                    val typeToken = TypeToken.get(field.genericType)
                    val adapter = gson.getAdapter(typeToken)
                    val value = adapter.fromJson(rawValue)

                    if (value == null) {
                        val restored = tryToRestoreNullValue(field, result)
                        if (!restored) setFieldOrError(field, result, null, name)
                    } else {
                        setFieldOrError(field, result, value, name)
                    }
                }
            }
            result.extraData = map

            return result
        } catch (e: Throwable) {
            throw ChatParsingError("custom object deserialisation error of $clazz", e)
        }
    }

    private fun setFieldOrError(field: Field, obj: Any, value: Any?, name: String) {
        try {
            field.set(obj, value)
        } catch (e: Throwable) {
            throw ChatParsingError("unable to set field $name with value $value")
        }
    }

    private fun tryToRestoreNullValue(
        field: Field,
        obj: Any
    ): Boolean {

        return when (field.type) {
            List::class.java -> {
                setFieldSafe(field, obj, ArrayList<Any>())
            }
            Map::class.java -> {
                setFieldSafe(field, obj, LinkedHashMap<Any, Any>())
            }
            Int::class.java -> {
                setFieldSafe(field, obj, 0)
            }
            Boolean::class.java -> {
                setFieldSafe(field, obj, false)
            }
            Float::class.java -> {
                setFieldSafe(field, obj, 0f)
            }
            String::class.java -> {
                setFieldSafe(field, obj, "")
            }
            else -> false
        }
    }

    private fun setFieldSafe(
        field: Field,
        obj: Any,
        value: Any
    ): Boolean {
        return try {
            field.set(obj, value)
            true
        } catch (e: Throwable) {
            logger.logE("unable to set field ${field.name} with value $value")
            false
        }
    }
}
