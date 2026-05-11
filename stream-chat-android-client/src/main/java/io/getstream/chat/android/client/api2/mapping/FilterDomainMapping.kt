/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NeutralFilterObject

/**
 * Parses a [Map] (as deserialized by Moshi from server JSON) into a [FilterObject], and also
 * returns the set of field names referenced by the filter (e.g. `last_message_at`,
 * `member_count`). Field names are collected during the same recursive walk that builds the
 * [FilterObject], so callers don't need a second pass over the parsed tree.
 *
 * Logical operators (`$and`/`$or`/`$nor`) and the `distinct`/`members` pair do not contribute to
 * the field set — only actual filter-field keys do.
 *
 * This is the reverse of [io.getstream.chat.android.client.parser.FilterObject.toMap].
 *
 * Returns `null` if the map is `null` or cannot be parsed.
 */
internal fun Map<String, Any>?.toFilterDomainWithFields(): Pair<FilterObject, Set<String>>? {
    if (this == null) return null
    val fields = mutableSetOf<String>()
    val filter = parseFilterMap(this, fields) ?: return null
    return filter to fields
}

@Suppress("ComplexMethod", "SpreadOperator")
private fun parseFilterMap(map: Map<String, Any>, fields: MutableSet<String>): FilterObject? {
    if (map.isEmpty()) return NeutralFilterObject

    if (map.size == 2 && map.containsKey(KEY_DISTINCT) && map.containsKey(KEY_MEMBERS)) {
        val memberIds = (map[KEY_MEMBERS] as? Collection<*>)?.filterIsInstance<String>() ?: return null
        return Filters.distinct(memberIds)
    }

    if (map.size == 1) {
        val (key, value) = map.entries.first()
        return parseSingleEntry(key, value, fields)
    }

    // Multi-key map: implicit AND
    val filters = map.entries.mapNotNull { (key, value) -> parseSingleEntry(key, value, fields) }
    if (filters.isEmpty()) return null
    if (filters.size == 1) return filters.first()
    return Filters.and(*filters.toTypedArray())
}

@Suppress("SpreadOperator")
private fun parseSingleEntry(key: String, value: Any, fields: MutableSet<String>): FilterObject? = when (key) {
    KEY_AND -> parseLogicalOperator(value, fields) { Filters.and(*it) }
    KEY_OR -> parseLogicalOperator(value, fields) { Filters.or(*it) }
    KEY_NOR -> parseLogicalOperator(value, fields) { Filters.nor(*it) }
    else -> parseFieldFilter(key, value, fields)
}

@Suppress("UNCHECKED_CAST")
private fun parseLogicalOperator(
    value: Any,
    fields: MutableSet<String>,
    factory: (Array<FilterObject>) -> FilterObject,
): FilterObject? {
    val list = value as? List<*> ?: return null
    val filters = list.mapNotNull { item ->
        (item as? Map<String, Any>)?.let { parseFilterMap(it, fields) }
    }
    if (filters.isEmpty()) return null
    return factory(filters.toTypedArray())
}

@Suppress("ComplexMethod", "DEPRECATION")
private fun parseFieldFilter(fieldName: String, value: Any, fields: MutableSet<String>): FilterObject? {
    fields.add(fieldName)
    if (value !is Map<*, *>) {
        return Filters.eq(fieldName, normalizeValue(value))
    }

    @Suppress("UNCHECKED_CAST")
    val operatorMap = value as Map<String, Any>
    if (operatorMap.isEmpty()) return null
    val (opKey, opValue) = operatorMap.entries.first()

    return when (opKey) {
        KEY_EQUALS -> Filters.eq(fieldName, normalizeValue(opValue))
        KEY_NOT_EQUALS -> Filters.ne(fieldName, normalizeValue(opValue))
        KEY_GREATER_THAN -> Filters.greaterThan(fieldName, normalizeValue(opValue))
        KEY_GREATER_THAN_OR_EQUALS -> Filters.greaterThanEquals(fieldName, normalizeValue(opValue))
        KEY_LESS_THAN -> Filters.lessThan(fieldName, normalizeValue(opValue))
        KEY_LESS_THAN_OR_EQUALS -> Filters.lessThanEquals(fieldName, normalizeValue(opValue))
        KEY_IN -> {
            val values = (opValue as? Collection<*>)?.map { normalizeValue(it ?: return null) } ?: return null
            Filters.`in`(fieldName, values)
        }
        KEY_NOT_IN -> {
            val values = (opValue as? Collection<*>)?.map { normalizeValue(it ?: return null) } ?: return null
            Filters.nin(fieldName, values)
        }
        KEY_CONTAINS -> Filters.contains(fieldName, normalizeValue(opValue))
        KEY_EXIST -> when (opValue as? Boolean) {
            true -> Filters.exists(fieldName)
            false -> Filters.notExists(fieldName)
            null -> null
        }
        KEY_AUTOCOMPLETE -> {
            val strValue = opValue as? String ?: return null
            Filters.autocomplete(fieldName, strValue)
        }
        else -> null
    }
}

private fun normalizeValue(value: Any): Any = when {
    value is Double && value == value.toLong().toDouble() -> {
        val longVal = value.toLong()
        if (longVal in Int.MIN_VALUE..Int.MAX_VALUE) longVal.toInt() else longVal
    }
    value is List<*> -> value.map { if (it != null) normalizeValue(it) else it }
    else -> value
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
