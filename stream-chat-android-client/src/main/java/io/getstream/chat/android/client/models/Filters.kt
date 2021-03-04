package io.getstream.chat.android.client.models

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

    @JvmStatic
    public fun neutral(): FilterObject = NeutralFilterObject

    @JvmStatic
    public fun exists(fieldName: String): FilterObject = ExistsFilterObject(fieldName)

    @JvmStatic
    public fun notExists(fieldName: String): FilterObject = NonExistsFilterObject(fieldName)

    @JvmStatic
    public fun contains(fieldName: String, value: Any): FilterObject = ContainsFilterObject(fieldName, value)

    @JvmStatic
    public fun and(vararg filters: FilterObject): FilterObject = AndFilterObject(filters.toSet())

    @JvmStatic
    public fun or(vararg filters: FilterObject): FilterObject = OrFilterObject(filters.toSet())

    @JvmStatic
    public fun nor(vararg filters: FilterObject): FilterObject = NorFilterObject(filters.toSet())

    @JvmStatic
    public fun eq(fieldName: String, value: Any): FilterObject = EqualsFilterObject(fieldName, value)

    @JvmStatic
    public fun ne(fieldName: String, value: Any): FilterObject = NotEqualsFilterObject(fieldName, value)

    @JvmStatic
    public fun greaterThan(fieldName: String, value: Any): FilterObject = GreaterThanFilterObject(fieldName, value)

    @JvmStatic
    public fun greaterThanEquals(fieldName: String, value: Any): FilterObject = GreaterThanOrEqualsFilterObject(fieldName, value)

    @JvmStatic
    public fun lessThan(fieldName: String, value: Any): FilterObject = LessThanFilterObject(fieldName, value)

    @JvmStatic
    public fun lessThanEquals(fieldName: String, value: Any): FilterObject = LessThanOrEqualsFilterObject(fieldName, value)

    @JvmStatic
    public fun `in`(fieldName: String, vararg values: String): FilterObject = InFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun `in`(fieldName: String, values: List<Any>): FilterObject = InFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun `in`(fieldName: String, vararg values: Number): FilterObject = InFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun nin(fieldName: String, vararg values: String): FilterObject = NotInFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun nin(fieldName: String, values: List<Any>): FilterObject = NotInFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun nin(fieldName: String, vararg values: Number): FilterObject = NotInFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun autocomplete(fieldName: String, value: String): FilterObject = AutocompleteFilterObject(fieldName, value)

    @JvmStatic
    public fun distinct(memberIds: List<String>): FilterObject = DistinctFilterObject(memberIds.toSet())
}
