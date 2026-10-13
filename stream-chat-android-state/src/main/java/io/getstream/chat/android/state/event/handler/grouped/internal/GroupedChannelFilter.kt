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

import io.getstream.chat.android.models.AndFilterObject
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.EqualsFilterObject
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.GreaterThanOrEqualsFilterObject
import io.getstream.chat.android.models.InFilterObject
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.NotEqualsFilterObject
import io.getstream.chat.android.models.OrFilterObject
import java.util.Date
import java.util.concurrent.TimeUnit

private const val QUERY_TIMESTAMP_FIELD_NAME = "query_timestamp"
private const val RELATIVE_DATE_PREFIX = "now-"
private const val RELATIVE_DATE_DAYS_SUFFIX = "d"

/**
 * The time grouped channel filters compare against: the latest of the channel's creation and its last message.
 */
internal val Channel.queryTimestamp: Date?
    get() = lastUpdated

/**
 * Whether a channel with the given [group] and [queryTimestamp] matches this grouped channels filter at [now].
 *
 * Evaluates the conditions the grouped channels endpoint accepts: `group` with `eq`, `in` and `ne`, and
 * `query_timestamp` with `greaterThanEquals` given as a [Date] or as a relative `now-7d` / `7d` value, combined
 * with `and` and `or`. Any other condition matches, so the server decides on the next query.
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

private fun cutoffOrNull(value: Any, now: Date): Date? = when (value) {
    is Date -> value
    is String -> value.removePrefix(RELATIVE_DATE_PREFIX)
        .takeIf { it.endsWith(RELATIVE_DATE_DAYS_SUFFIX) }
        ?.removeSuffix(RELATIVE_DATE_DAYS_SUFFIX)
        ?.toLongOrNull()
        ?.let { days -> Date(now.time - TimeUnit.DAYS.toMillis(days)) }
    else -> null
}
