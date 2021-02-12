package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.utils.FilterObject

/**
 * Stream supports a limited set of filters for querying channels, users and members.
 * The example below shows how to filter for channels of type messaging where the current
 * user is a member
 *
 * @code
 * val filter = Filters.and(
 *     Filters.eq("type", "messaging"),
 *     Filters.`in`("members", listOf(user.id))
 * )
 *
 * See <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin" target="_top">Query Channels Documentation</a>
 */
public object Filters {

    internal const val KEY_EXIST = "\$exists"
    internal const val KEY_CONTAINS = "\$contains"
    internal const val KEY_AND = "\$and"
    internal const val KEY_OR = "\$or"
    internal const val KEY_NOR = "\$nor"
    internal const val KEY_NE = "\$ne"
    internal const val KEY_GREATER_THAN = "\$gt"
    internal const val KEY_GREATER_THAN_OR_EQUALS = "\$gte"
    internal const val KEY_LESS_THAN = "\$lt"
    internal const val KEY_LESS_THAN_OR_EQUALS: String = "\$lte"
    internal const val KEY_IN = "\$in"
    internal const val KEY_NOT_IN = "\$nin"
    internal const val KEY_AUTOCOMPLETE = "\$autocomplete"

    @JvmStatic
    public fun exists(value: Any): FilterObject {
        return FilterObject(KEY_EXIST, value)
    }

    @JvmStatic
    public fun contains(value: Any): FilterObject {
        return FilterObject(KEY_CONTAINS, value)
    }

    @JvmStatic
    public fun contains(vararg filters: FilterObject): FilterObject {
        return FilterObject(KEY_CONTAINS, filters)
    }

    @JvmStatic
    public fun and(vararg filters: FilterObject): FilterObject {
        return FilterObject(KEY_AND, filters)
    }

    @JvmStatic
    public fun or(vararg filters: FilterObject): FilterObject {
        return FilterObject(KEY_OR, filters)
    }

    @JvmStatic
    public fun nor(vararg filters: FilterObject): FilterObject {
        return FilterObject(KEY_NOR, filters)
    }

    @JvmStatic
    public fun eq(field: String, value: Any): FilterObject {
        return FilterObject(field, value)
    }

    @JvmStatic
    public fun ne(field: String, value: Any): FilterObject {
        return FilterObject(field, FilterObject(KEY_NE, value))
    }

    @JvmStatic
    public fun greaterThan(field: String, value: Any): FilterObject {
        return FilterObject(field, FilterObject(KEY_GREATER_THAN, value))
    }

    @JvmStatic
    public fun greaterThanEquals(field: String, value: Any): FilterObject {
        return FilterObject(field, FilterObject(KEY_GREATER_THAN_OR_EQUALS, value))
    }

    @JvmStatic
    public fun lessThan(field: String, value: Any): FilterObject {
        return FilterObject(field, FilterObject(KEY_LESS_THAN, value))
    }

    @JvmStatic
    public fun lessThanEquals(field: String, value: Any): FilterObject {
        return FilterObject(field, FilterObject(KEY_LESS_THAN_OR_EQUALS, value))
    }

    @JvmStatic
    public fun `in`(field: String, vararg values: String): FilterObject {
        return FilterObject(field, FilterObject(KEY_IN, values))
    }

    @JvmStatic
    public fun `in`(field: String, values: List<*>): FilterObject {
        return FilterObject(field, FilterObject(KEY_IN, values))
    }

    @JvmStatic
    public fun `in`(field: String, vararg values: Number): FilterObject {
        return FilterObject(field, FilterObject(KEY_IN, values))
    }

    @JvmStatic
    public fun nin(field: String, vararg values: String): FilterObject {
        return FilterObject(field, FilterObject(KEY_NOT_IN, values))
    }

    @JvmStatic
    public fun nin(field: String, values: List<*>): FilterObject {
        return FilterObject(field, FilterObject(KEY_NOT_IN, values))
    }

    @JvmStatic
    public fun nin(field: String, vararg values: Number): FilterObject {
        return FilterObject(field, FilterObject(KEY_NOT_IN, values))
    }

    @JvmStatic
    public fun autocomplete(field: String, value: String): FilterObject {
        return FilterObject(field, FilterObject(KEY_AUTOCOMPLETE, value))
    }
}
