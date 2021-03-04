package io.getstream.chat.android.livedata.repository.database.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.api.models.AndFilterObject
import io.getstream.chat.android.client.api.models.AutocompleteFilterObject
import io.getstream.chat.android.client.api.models.ContainsFilterObject
import io.getstream.chat.android.client.api.models.DistinctFilterObject
import io.getstream.chat.android.client.api.models.EqualsFilterObject
import io.getstream.chat.android.client.api.models.ExistsFilterObject
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.GreaterThanFilterObject
import io.getstream.chat.android.client.api.models.GreaterThanOrEqualsFilterObject
import io.getstream.chat.android.client.api.models.InFilterObject
import io.getstream.chat.android.client.api.models.LessThanFilterObject
import io.getstream.chat.android.client.api.models.LessThanOrEqualsFilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.NonExistsFilterObject
import io.getstream.chat.android.client.api.models.NorFilterObject
import io.getstream.chat.android.client.api.models.NotEqualsFilterObject
import io.getstream.chat.android.client.api.models.NotInFilterObject
import io.getstream.chat.android.client.api.models.OrFilterObject
import io.getstream.chat.android.livedata.gson
import java.lang.IllegalArgumentException
import java.lang.reflect.Type

internal class FilterObjectConverter {
    @TypeConverter
    fun stringToObject(data: String?): FilterObject {
        if (data.isNullOrEmpty() || data == "null") {
            return NeutralFilterObject
        }
        val hashType: Type = object : TypeToken<HashMap<String, Any>?>() {}.type
        val dataMap: HashMap<String, Any> = gson.fromJson(data, hashType)
        return dataMap.toFilterObject()
    }

    @TypeConverter
    fun objectToString(filterObject: FilterObject): String {
        return gson.getAdapter(Map::class.java).toJson(filterObject.toMap())
    }
}

private fun Map<String, Any>.toFilterObject(): FilterObject = when {
    this.isEmpty() -> NeutralFilterObject
    this.size == 1 -> this.entries.first().toFilterObject()
    this.size == 2 && this.containsKey("distinct") && this.containsKey("members") -> DistinctFilterObject((this["members"] as List<String>).toSet())
    else -> throw IllegalArgumentException("FilterObject can be create with this map `$this`")
}

private fun Map.Entry<String, Any>.toFilterObject(): FilterObject = when (this.key) {
    "\$and" -> AndFilterObject((this.value as List<Map<String, Any>>).map(Map<String, Any>::toFilterObject).toSet())
    "\$or" -> OrFilterObject((this.value as List<Map<String, Any>>).map(Map<String, Any>::toFilterObject).toSet())
    "\$nor" -> NorFilterObject((this.value as List<Map<String, Any>>).map(Map<String, Any>::toFilterObject).toSet())
    else -> (this.value as Map<String, Any>).entries.first().let {
        when {
            it.key == "\$exists" -> when (it.value as Boolean) {
                true -> ExistsFilterObject(this.key)
                false -> NonExistsFilterObject(this.key)
            }
            it.key == "\$eq" -> EqualsFilterObject(this.key, it.value)
            it.key == "\$ne" -> NotEqualsFilterObject(this.key, it.value)
            it.key == "\$contains" -> ContainsFilterObject(this.key, it.value)
            it.key == "\$gt" -> GreaterThanFilterObject(this.key, it.value)
            it.key == "\$gte" -> GreaterThanOrEqualsFilterObject(this.key, it.value)
            it.key == "\$lt" -> LessThanFilterObject(this.key, it.value)
            it.key == "\$lte" -> LessThanOrEqualsFilterObject(this.key, it.value)
            it.key == "\$in" -> InFilterObject(this.key, (it.value as List<Any>).toSet())
            it.key == "\$nin" -> NotInFilterObject(this.key, (it.value as List<Any>).toSet())
            it.key == "\$autocomplete" -> AutocompleteFilterObject(this.key, it.value as String)
            else -> throw IllegalArgumentException("FilterObject can be create with this map `$this`")
        }
    }
}

private fun FilterObject.toMap(): Map<*, *> = when (this) {
    is AndFilterObject -> mapOf("\$and" to this.filterObjects.map(FilterObject::toMap))
    is OrFilterObject -> mapOf("\$or" to this.filterObjects.map(FilterObject::toMap))
    is NorFilterObject -> mapOf("\$nor" to this.filterObjects.map(FilterObject::toMap))
    is ExistsFilterObject -> mapOf(this.fieldName to mapOf("\$exists" to true))
    is NonExistsFilterObject -> mapOf(this.fieldName to mapOf("\$exists" to false))
    is EqualsFilterObject -> mapOf(this.fieldName to mapOf("\$eq" to this.value))
    is NotEqualsFilterObject -> mapOf(this.fieldName to mapOf("\$ne" to this.value))
    is ContainsFilterObject -> mapOf(this.fieldName to mapOf("\$contains" to this.value))
    is GreaterThanFilterObject -> mapOf(this.fieldName to mapOf("\$gt" to this.value))
    is GreaterThanOrEqualsFilterObject -> mapOf(this.fieldName to mapOf("\$gte" to this.value))
    is LessThanFilterObject -> mapOf(this.fieldName to mapOf("\$lt" to this.value))
    is LessThanOrEqualsFilterObject -> mapOf(this.fieldName to mapOf("\$lte" to this.value))
    is InFilterObject -> mapOf(this.fieldName to mapOf("\$in" to this.values))
    is NotInFilterObject -> mapOf(this.fieldName to mapOf("\$nin" to this.values))
    is AutocompleteFilterObject -> mapOf(this.fieldName to mapOf("\$autocomplete" to this.value))
    is DistinctFilterObject -> mapOf("distinct" to true, "members" to this.memberIds)
    is NeutralFilterObject -> emptyMap<String, String>()
}
