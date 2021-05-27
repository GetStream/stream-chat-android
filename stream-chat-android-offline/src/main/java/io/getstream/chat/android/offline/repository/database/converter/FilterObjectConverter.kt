package io.getstream.chat.android.offline.repository.database.converter

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
import io.getstream.chat.android.client.api.models.NorFilterObject
import io.getstream.chat.android.client.api.models.NotEqualsFilterObject
import io.getstream.chat.android.client.api.models.NotExistsFilterObject
import io.getstream.chat.android.client.api.models.NotInFilterObject
import io.getstream.chat.android.client.api.models.OrFilterObject
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.gson
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
    this.size == 2 && this.containsKey(KEY_DISTINCT) && this.containsKey(KEY_MEMBERS) -> Filters.distinct((this[KEY_MEMBERS] as List<String>))
    else -> throw IllegalArgumentException("FilterObject can be create with this map `$this`")
}

private fun Map.Entry<String, Any>.toFilterObject(): FilterObject = when (this.key) {
    KEY_AND -> Filters.and(*(this.value as List<Map<String, Any>>).map(Map<String, Any>::toFilterObject).toTypedArray())
    KEY_OR -> Filters.or(*(this.value as List<Map<String, Any>>).map(Map<String, Any>::toFilterObject).toTypedArray())
    KEY_NOR -> Filters.nor(*(this.value as List<Map<String, Any>>).map(Map<String, Any>::toFilterObject).toTypedArray())
    else -> (this.value as Map<String, Any>).entries.first().let {
        when (it.key) {
            KEY_EXIST -> when (it.value as Boolean) {
                true -> Filters.exists(this.key)
                false -> Filters.notExists(this.key)
            }
            KEY_EQUALS -> Filters.eq(this.key, it.value)
            KEY_NOT_EQUALS -> Filters.ne(this.key, it.value)
            KEY_CONTAINS -> Filters.contains(this.key, it.value)
            KEY_GREATER_THAN -> Filters.greaterThan(this.key, it.value)
            KEY_GREATER_THAN_OR_EQUALS -> Filters.greaterThanEquals(this.key, it.value)
            KEY_LESS_THAN -> Filters.lessThan(this.key, it.value)
            KEY_LESS_THAN_OR_EQUALS -> Filters.lessThanEquals(this.key, it.value)
            KEY_IN -> Filters.`in`(this.key, (it.value as List<Any>))
            KEY_NOT_IN -> Filters.nin(this.key, (it.value as List<Any>))
            KEY_AUTOCOMPLETE -> Filters.autocomplete(this.key, it.value as String)
            else -> throw IllegalArgumentException("FilterObject can be create with this map `$this`")
        }
    }
}

private fun FilterObject.toMap(): Map<*, *> = when (this) {
    is AndFilterObject -> mapOf(KEY_AND to this.filterObjects.map(FilterObject::toMap))
    is OrFilterObject -> mapOf(KEY_OR to this.filterObjects.map(FilterObject::toMap))
    is NorFilterObject -> mapOf(KEY_NOR to this.filterObjects.map(FilterObject::toMap))
    is ExistsFilterObject -> mapOf(this.fieldName to mapOf(KEY_EXIST to true))
    is NotExistsFilterObject -> mapOf(this.fieldName to mapOf(KEY_EXIST to false))
    is EqualsFilterObject -> mapOf(this.fieldName to mapOf(KEY_EQUALS to this.value))
    is NotEqualsFilterObject -> mapOf(this.fieldName to mapOf(KEY_NOT_EQUALS to this.value))
    is ContainsFilterObject -> mapOf(this.fieldName to mapOf(KEY_CONTAINS to this.value))
    is GreaterThanFilterObject -> mapOf(this.fieldName to mapOf(KEY_GREATER_THAN to this.value))
    is GreaterThanOrEqualsFilterObject -> mapOf(this.fieldName to mapOf(KEY_GREATER_THAN_OR_EQUALS to this.value))
    is LessThanFilterObject -> mapOf(this.fieldName to mapOf(KEY_LESS_THAN to this.value))
    is LessThanOrEqualsFilterObject -> mapOf(this.fieldName to mapOf(KEY_LESS_THAN_OR_EQUALS to this.value))
    is InFilterObject -> mapOf(this.fieldName to mapOf(KEY_IN to this.values))
    is NotInFilterObject -> mapOf(this.fieldName to mapOf(KEY_NOT_IN to this.values))
    is AutocompleteFilterObject -> mapOf(this.fieldName to mapOf(KEY_AUTOCOMPLETE to this.value))
    is DistinctFilterObject -> mapOf(KEY_DISTINCT to true, KEY_MEMBERS to this.memberIds)
    is NeutralFilterObject -> emptyMap<String, String>()
}

private const val KEY_EXIST: String = "\$exists"
private const val KEY_CONTAINS: String = "\$contains"
private const val KEY_AND: String = "\$and"
private const val KEY_OR: String = "\$or"
private const val KEY_NOR: String = "\$nor"
private const val KEY_EQUALS: String = "\$eq"
private const val KEY_NOT_EQUALS: String = "\$ne"
private const val KEY_GREATER_THAN: String = "\$gt"
private const val KEY_GREATER_THAN_OR_EQUALS: String = "\$gte"
private const val KEY_LESS_THAN: String = "\$lt"
private const val KEY_LESS_THAN_OR_EQUALS: String = "\$lte"
private const val KEY_IN: String = "\$in"
private const val KEY_NOT_IN: String = "\$nin"
private const val KEY_AUTOCOMPLETE: String = "\$autocomplete"
private const val KEY_DISTINCT: String = "distinct"
private const val KEY_MEMBERS: String = "members"
