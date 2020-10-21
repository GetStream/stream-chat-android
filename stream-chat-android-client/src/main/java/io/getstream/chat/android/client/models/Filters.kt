package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.utils.FilterObject

public object Filters {

    @JvmStatic
    public fun exists(value: Any): FilterObject {
        return FilterObject(
            "\$exists",
            value
        )
    }

    @JvmStatic
    public fun contains(value: Any): FilterObject {
        return FilterObject(
            "\$contains",
            value
        )
    }

    @JvmStatic
    public fun contains(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$contains",
            filters
        )
    }

    @JvmStatic
    public fun and(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$and",
            filters
        )
    }

    @JvmStatic
    public fun or(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$or",
            filters
        )
    }

    @JvmStatic
    public fun nor(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$nor",
            filters
        )
    }

    @JvmStatic
    public fun eq(field: String, value: Any): FilterObject {
        return FilterObject(field, value)
    }

    @JvmStatic
    public fun ne(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$ne", value)
        )
    }

    @JvmStatic
    public fun greaterThan(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$gt", value)
        )
    }

    @JvmStatic
    public fun greaterThanEquals(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$gte", value)
        )
    }

    @JvmStatic
    public fun lessThan(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$lt", value)
        )
    }

    @JvmStatic
    public fun lessThanEquals(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$lte", value)
        )
    }

    @JvmStatic
    public fun `in`(field: String, vararg values: String): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    @JvmStatic
    public fun `in`(field: String, values: List<*>): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    @JvmStatic
    public fun `in`(field: String, vararg values: Number): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    @JvmStatic
    public fun nin(field: String, vararg values: String): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }

    @JvmStatic
    public fun nin(field: String, values: List<*>): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }

    @JvmStatic
    public fun nin(field: String, vararg values: Number): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }

    @JvmStatic
    public fun autocomplete(field: String, value: String): FilterObject {
        return FilterObject(field, FilterObject("\$autocomplete", value))
    }
}
