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

package io.getstream.chat.android.models

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
 * See<a href="https://getstream.io/chat/docs/query_channels/?language=kotlin" target="_top">
 *     Query Channels Documentation</a>
 */
@Suppress("TooManyFunctions")
public object Filters {

    @JvmStatic
    public fun neutral(): FilterObject = NeutralFilterObject

    @JvmStatic
    public fun exists(fieldName: String): FilterObject = ExistsFilterObject(fieldName)

    @JvmStatic
    public fun notExists(fieldName: String): FilterObject = NotExistsFilterObject(fieldName)

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
    @Deprecated(
        message = "The notEquals filter is inefficient and causes performance issues. It will not be supported in the" +
            "future. Feel free to contact our Customer Support to get help finding an appropriate alternative " +
            "solution for your integration.",
        level = DeprecationLevel.WARNING,
    )
    public fun ne(fieldName: String, value: Any): FilterObject = NotEqualsFilterObject(fieldName, value)

    @JvmStatic
    public fun greaterThan(fieldName: String, value: Any): FilterObject = GreaterThanFilterObject(fieldName, value)

    @JvmStatic
    public fun greaterThanEquals(fieldName: String, value: Any): FilterObject =
        GreaterThanOrEqualsFilterObject(fieldName, value)

    @JvmStatic
    public fun lessThan(fieldName: String, value: Any): FilterObject = LessThanFilterObject(fieldName, value)

    @JvmStatic
    public fun lessThanEquals(fieldName: String, value: Any): FilterObject =
        LessThanOrEqualsFilterObject(fieldName, value)

    @JvmStatic
    public fun `in`(fieldName: String, vararg values: String): FilterObject = InFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun `in`(fieldName: String, values: List<Any>): FilterObject = InFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun `in`(fieldName: String, vararg values: Number): FilterObject = InFilterObject(fieldName, values.toSet())

    @JvmStatic
    @Deprecated(
        message = "This filter will stop to be supported in the future.",
        level = DeprecationLevel.WARNING,
    )
    public fun nin(fieldName: String, vararg values: String): FilterObject =
        NotInFilterObject(fieldName, values.toSet())

    @JvmStatic
    @Deprecated(
        message = "This filter will stop to be supported in the future.",
        level = DeprecationLevel.WARNING,
    )
    public fun nin(fieldName: String, values: List<Any>): FilterObject = NotInFilterObject(fieldName, values.toSet())

    @JvmStatic
    @Deprecated(
        message = "This filter will stop to be supported in the future.",
        level = DeprecationLevel.WARNING,
    )
    public fun nin(fieldName: String, vararg values: Number): FilterObject =
        NotInFilterObject(fieldName, values.toSet())

    @JvmStatic
    public fun autocomplete(fieldName: String, value: String): FilterObject = AutocompleteFilterObject(fieldName, value)

    @JvmStatic
    public fun distinct(memberIds: List<String>): FilterObject = DistinctFilterObject(memberIds.toSet())
}
