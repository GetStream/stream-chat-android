/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
 * Filter object that specifies requests for backend queries.
 */
public sealed class FilterObject
public data class AndFilterObject internal constructor(val filterObjects: Set<FilterObject>) : FilterObject()
public data class OrFilterObject internal constructor(val filterObjects: Set<FilterObject>) : FilterObject()
public data class NorFilterObject internal constructor(val filterObjects: Set<FilterObject>) : FilterObject()
public data class ContainsFilterObject internal constructor(
    val fieldName: String,
    val value: Any,
) : FilterObject()
public data class AutocompleteFilterObject internal constructor(
    val fieldName: String,
    val value: String,
) : FilterObject()
public data class ExistsFilterObject internal constructor(val fieldName: String) : FilterObject()
public data class NotExistsFilterObject internal constructor(val fieldName: String) : FilterObject()
public data class EqualsFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class NotEqualsFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class GreaterThanFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class GreaterThanOrEqualsFilterObject internal constructor(
    val fieldName: String,
    val value: Any,
) : FilterObject()
public data class LessThanFilterObject internal constructor(val fieldName: String, val value: Any) : FilterObject()
public data class LessThanOrEqualsFilterObject internal constructor(
    val fieldName: String,
    val value: Any,
) : FilterObject()
public data class InFilterObject internal constructor(val fieldName: String, val values: Set<Any>) : FilterObject()
public data class DistinctFilterObject internal constructor(val memberIds: Set<String>) : FilterObject()
public object NeutralFilterObject : FilterObject()
