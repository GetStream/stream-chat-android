package io.getstream.chat.android.client.parser.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.models.Channel

class ChannelGsonAdaper(val gson: Gson) : TypeAdapter<Channel>() {

    override fun write(writer: JsonWriter, channel: Channel?) {

        if (channel == null) {
            gson.getAdapter(HashMap::class.java).write(writer, null)
        } else {
            val result = HashMap<String, Any>()
            channel.extraData.map { result[it.key] = it.value }

            Channel::class.java.declaredFields.forEach {

                val value = it.get(channel)
                result[it.name] = gson.toJson(value)
            }

            gson.getAdapter(HashMap::class.java).write(writer, result)
        }


    }

    override fun read(reader: JsonReader): Channel {
        val result = Channel()
        val map = gson.getAdapter(HashMap::class.java).read(reader) as HashMap<String, Any>
        Channel::class.java.declaredFields.forEach { field ->
            
            var name = field.name
            val annotation = field.getAnnotation(SerializedName::class.java)
            if (annotation != null) name = annotation.value

            if (map.containsKey(name)) {
                val json = gson.toJson(map.remove(name))
                val finalValue = gson.getAdapter(field.type).fromJson(json)
                field.isAccessible = true
                field.set(result, finalValue)
            }
        }
        result.extraData = map

        return result
    }
}