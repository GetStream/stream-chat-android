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

package io.getstream.chat.android.network.infrastructure

import android.util.LruCache
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Maps ISO-8601 date-time strings to [ExactDate], keeping the string as sent.
 */
internal class ExactDateAdapter {

    private val cache = LruCache<String, ExactDate>(CACHE_SIZE)

    @ToJson
    internal fun toJson(value: ExactDate): String = value.raw

    @FromJson
    internal fun fromJson(value: String): ExactDate? {
        if (value.isEmpty()) return null
        cache.get(value)?.let { return it }
        val parsed = ExactDate.parseOrNull(value) ?: return null
        cache.put(value, parsed)
        return parsed
    }

    private companion object {
        const val CACHE_SIZE = 300
    }
}
