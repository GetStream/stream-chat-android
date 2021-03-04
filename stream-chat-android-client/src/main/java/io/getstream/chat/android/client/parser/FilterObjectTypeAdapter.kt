package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
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

internal class FilterObjectTypeAdapter(val gson: Gson) : TypeAdapter<FilterObject>() {

    override fun write(out: JsonWriter, filterObject: FilterObject) {
        val adapter = gson.getAdapter(Map::class.java)
        adapter.write(out, filterObject.toMap())
    }

    override fun read(reader: JsonReader): FilterObject? {
        return null
    }
}

private fun FilterObject.toMap(): Map<*, *> = when (this) {
    is AndFilterObject -> mapOf("\$and" to this.filterObjects.map(FilterObject::toMap))
    is OrFilterObject -> mapOf("\$or" to this.filterObjects.map(FilterObject::toMap))
    is NorFilterObject -> mapOf("\$nor" to this.filterObjects.map(FilterObject::toMap))
    is ExistsFilterObject -> mapOf(this.fieldName to mapOf("\$exists" to true))
    is NonExistsFilterObject -> mapOf(this.fieldName to mapOf("\$exists" to false))
    is EqualsFilterObject -> mapOf(this.fieldName to this.value)
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
