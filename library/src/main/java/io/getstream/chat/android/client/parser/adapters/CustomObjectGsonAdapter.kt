package io.getstream.chat.android.client.parser.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.api.models.CustomObject
import io.getstream.chat.android.client.errors.ChatParsingError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation


class CustomObjectGsonAdapter(val gson: Gson, val clazz: Class<*>) : TypeAdapter<CustomObject>() {

    companion object {
        val TAG = CustomObjectGsonAdapter::class.java.simpleName
    }

    override fun write(writer: JsonWriter, obj: CustomObject?) {

        try {
            if (obj == null) {
                gson.getAdapter(HashMap::class.java).write(writer, null)
            } else {
                val result = HashMap<String, Any>()
                obj.extraData.map { result[it.key] = it.value }

                for (field in clazz.declaredFields) {
                    if (field.isSynthetic) continue
                    if (field.getAnnotation(IgnoreSerialisation::class.java) != null) continue

                    field.isAccessible = true
                    val name = field.name
                    val value = field.get(obj)

                    if (value == null) continue
                    result[name] = value
                }

                gson.getAdapter(HashMap::class.java).write(writer, result)
            }
        } catch (e: Exception) {
            throw ChatParsingError("custom object serialisation error", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun read(reader: JsonReader): CustomObject? {

        try {
            val result = clazz.newInstance() as CustomObject
            val read = gson.getAdapter(HashMap::class.java).read(reader)

            if (read == null) {
                ChatLogger.instance?.logE(
                    TAG,
                    "exception case when api returned null where it shouldn't: $reader"
                )
                return null
            }

            val map = read as HashMap<String, Any>
            for (field in clazz.declaredFields) {

                if (field.isSynthetic) continue
                if (field.getAnnotation(IgnoreDeserialisation::class.java) != null) continue

                var name = field.name
                val serializedName = field.getAnnotation(SerializedName::class.java)
                if (serializedName != null) name = serializedName.value

                if (map.containsKey(name)) {
                    field.isAccessible = true
                    val rawValue = gson.toJson(map.remove(name))
                    val value = gson.getAdapter(field.type).fromJson(rawValue)
                    if (value == null) continue
                    field.set(result, value)
                }
            }
            result.extraData = map

            return result
        } catch (e: Exception) {
            throw ChatParsingError("custom object deserialisation error", e)
        }
    }
}