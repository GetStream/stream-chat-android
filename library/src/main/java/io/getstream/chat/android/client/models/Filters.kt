package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.utils.FilterObject

object Filters {

    @JvmStatic
    fun contains(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$contains",
            filters
        )
    }

    @JvmStatic
    fun and(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$and",
            filters
        )
    }

    @JvmStatic
    fun or(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$or",
            filters
        )
    }

    @JvmStatic
    fun nor(vararg filters: FilterObject): FilterObject {
        return FilterObject(
            "\$nor",
            filters
        )
    }

    @JvmStatic
    fun eq(field: String, value: Any): FilterObject {
        return FilterObject(field, value)
    }

    @JvmStatic
    fun ne(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$ne", value)
        )
    }

    @JvmStatic
    fun greaterThan(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$gt", value)
        )
    }

    @JvmStatic
    fun greaterThanEquals(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$gte", value)
        )
    }

    @JvmStatic
    fun lessThan(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$lt", value)
        )
    }

    @JvmStatic
    fun lessThanEquals(field: String, value: Any): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$lte", value)
        )
    }

    @JvmStatic
    fun `in`(field: String, vararg values: String): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    @JvmStatic
    fun `in`(field: String, values: List<*>): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    @JvmStatic
    fun `in`(field: String, vararg values: Number): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$in", values)
        )
    }

    @JvmStatic
    fun nin(field: String, vararg values: String): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }

    @JvmStatic
    fun nin(field: String, values: List<*>): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }

    @JvmStatic
    fun nin(field: String, vararg values: Number): FilterObject {
        return FilterObject(
            field,
            FilterObject("\$nin", values)
        )
    }
}
