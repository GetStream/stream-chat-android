package io.getstream.chat.android.core.poc.library.api

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import io.getstream.chat.android.core.poc.library.Device
import io.getstream.chat.android.core.poc.library.Mute
import io.getstream.chat.android.core.poc.library.User
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


class UserGsonAdapter(val gson: Gson) : TypeAdapter<User>() {

    @Throws(IOException::class)
    override fun write(writer: com.google.gson.stream.JsonWriter, user: User) {
        val data: HashMap<String, Any> = HashMap()

        user.extraData.map { data[it.key] = it.value }

        data["id"] = user.id
        data["name"] = user.name
        data["image"] = user.image
        val adapter = gson.getAdapter(HashMap::class.java)
        adapter.write(writer, data)
    }

    override fun read(reader: com.google.gson.stream.JsonReader): User {

        val adapter: TypeAdapter<*> = gson.getAdapter(HashMap::class.java)

        val value = adapter.read(reader) as HashMap<String, Any>
        val user = User("")
        val extraData: HashMap<String, Any> = HashMap()
        for (set in value.entries) {
            val json = gson.toJson(set.value)
            when (set.key) {
                "id" -> {
                    user.id = set.value as String
                }
                "name" -> {
                    user.name = set.value as String
                }
                "image" -> {
                    user.image = set.value as String
                }
                "role" -> {
                    user.role = set.value as String
                }
                "created_at" -> {
                    user.created_at = Date(set.value as Long)
                }
                "updated_at" -> {
                    user.updated_at = Date(set.value as Long)
                }
                "last_active" -> {
                    user.last_active = Date(set.value as Long)
                }
                "online" -> {
                    user.online = set.value as Boolean
                }
                "banned" -> {
                    user.banned = set.value as Boolean
                }
                "total_unread_count" -> {
                    user.totalUnreadCount = set.value as Int
                }
                "unread_channels" -> {
                    user.unreadChannels = set.value as Int
                }
                "invisible" -> {
                    user.invisible = set.value as Boolean
                }
                "devices" -> {
                    user.devices = set.value as List<Device>
                }
                "mutes" -> {
                    user.mutes = set.value as List<Mute>
                }
            }
            // Set Extra Data
            extraData[set.key as String] = set.value
        }
        user.extraData = extraData
        return user
    }
}
