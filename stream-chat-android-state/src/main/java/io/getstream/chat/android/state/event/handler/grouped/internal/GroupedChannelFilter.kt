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

package io.getstream.chat.android.state.event.handler.grouped.internal

import io.getstream.chat.android.client.extensions.currentUserUnreadCount
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.core.utils.date.max
import io.getstream.chat.android.models.AndFilterObject
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.EqualsFilterObject
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.GreaterThanOrEqualsFilterObject
import io.getstream.chat.android.models.InFilterObject
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.NotEqualsFilterObject
import io.getstream.chat.android.models.OrFilterObject
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import java.util.Date
import java.util.concurrent.TimeUnit

private const val QUERY_TIMESTAMP_FIELD_NAME = "query_timestamp"
private const val TOUCHED_AT_FIELD_NAME = "touched_at"
private const val DISABLED_FIELD_NAME = "disabled"
private const val RELATIVE_DATE_PREFIX = "now-"
private const val RELATIVE_DATE_DAYS_SUFFIX = "d"

/**
 * The time grouped channel filters and sorts compare against: the latest of the channel's creation, its last message
 * and its custom `touched_at` date.
 */
internal val Channel.queryTimestamp: Date?
    get() = max(lastUpdated, touchedAt)

private val Channel.touchedAt: Date?
    get() = when (val value = extraData[TOUCHED_AT_FIELD_NAME]) {
        is Date -> value
        is String -> dateFormatter.parse(value)
        else -> null
    }

/**
 * Whether the channel counts towards a grouped unread badge for [userId]: it is not hidden or disabled and has unread
 * messages, as the backend counts it.
 */
internal fun Channel.isUnreadInGroupFor(userId: String): Boolean =
    hidden != true && !isDisabled && currentUserUnreadCount(userId) > 0

// The model has no field for it, so the server's `disabled` flag arrives in the extra data.
internal val Channel.isDisabled: Boolean
    get() = extraData[DISABLED_FIELD_NAME] == true

private val queryTimestampDescSort = QuerySortByField.descByName<Channel>(QUERY_TIMESTAMP_FIELD_NAME)

// One shared instance, so applying the same request again does not replace the sort and re-sort the list.
private val queryTimestampSort: QuerySorter<Channel> = QueryTimestampSort(queryTimestampDescSort)

/**
 * The local order for a grouped list requested with [sort]. A `query_timestamp` sort orders channels by
 * [queryTimestamp], newest first, because the field is computed on the device. Any other sort keeps [defaultSort].
 */
internal fun groupedLocalSort(sort: QuerySorter<Channel>?, defaultSort: QuerySorter<Channel>): QuerySorter<Channel> =
    if (sort == queryTimestampDescSort) queryTimestampSort else defaultSort

private class QueryTimestampSort(requested: QuerySorter<Channel>) : QuerySorter<Channel> by requested {
    override val comparator: Comparator<in Channel> = compareByDescending(Channel::queryTimestamp)
}

/**
 * Whether a channel with the given [group] and [queryTimestamp] matches this grouped channels filter at [now].
 *
 * Evaluates the conditions the grouped channels endpoint accepts: `group` with `eq`, `in` and `ne`, and
 * `query_timestamp` with `greaterThanEquals` given as a [Date], a full timestamp or a relative `now-7d` / `7d`
 * value, combined with `and` and `or`. Any other condition matches, so the server decides on the next query.
 */
internal fun FilterObject.matchesGroupedChannel(group: String?, queryTimestamp: Date?, now: Date): Boolean =
    when (this) {
        is NeutralFilterObject -> true
        is AndFilterObject -> filterObjects.all { it.matchesGroupedChannel(group, queryTimestamp, now) }
        is OrFilterObject -> filterObjects.any { it.matchesGroupedChannel(group, queryTimestamp, now) }
        is EqualsFilterObject -> fieldName != DEFAULT_GROUP_FIELD_NAME || value == group
        is InFilterObject -> fieldName != DEFAULT_GROUP_FIELD_NAME || values.any { it == group }
        is NotEqualsFilterObject -> fieldName != DEFAULT_GROUP_FIELD_NAME || value != group
        is GreaterThanOrEqualsFilterObject -> {
            val cutoff = cutoffOrNull(value, now)
            if (fieldName != QUERY_TIMESTAMP_FIELD_NAME || cutoff == null) {
                true
            } else {
                queryTimestamp != null && !queryTimestamp.before(cutoff)
            }
        }
        else -> true
    }

private val dateFormatter = StreamDateFormatter()

private fun cutoffOrNull(value: Any, now: Date): Date? = when (value) {
    is Date -> value
    is String -> relativeCutoffOrNull(value, now) ?: dateFormatter.parse(value)
    else -> null
}

private fun relativeCutoffOrNull(value: String, now: Date): Date? = value.removePrefix(RELATIVE_DATE_PREFIX)
    .takeIf { it.endsWith(RELATIVE_DATE_DAYS_SUFFIX) }
    ?.removeSuffix(RELATIVE_DATE_DAYS_SUFFIX)
    ?.toLongOrNull()
    ?.takeIf { days -> days > 0 }
    ?.let { days -> Date(now.time - TimeUnit.DAYS.toMillis(days)) }
