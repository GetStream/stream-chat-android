/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.network.infrastructure

import android.os.Build
import android.util.LruCache
import com.ethlo.time.ITU
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class IsoDateAdapter {

    // SimpleDateFormat is used on API < 26 where ITU's java.time return types aren't available.
    private val withMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val withoutMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val cache = LruCache<String, Date>(CACHE_SIZE)

    @ToJson
    @Synchronized
    fun toJson(value: Date): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ITU.formatUtcMilli(value.toInstant().atOffset(ZoneOffset.UTC))
        } else {
            withMillis.format(value)
        }
    }

    @FromJson
    @Synchronized
    fun fromJson(value: String): Date? {
        if (value.isEmpty()) return null
        cache.get(value)?.let { return it }
        val parsed = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Date.from(ITU.parseDateTime(value).toInstant())
            } else {
                withMillis.parse(value)
            }
        } catch (_: Throwable) {
            try {
                withoutMillis.parse(value)
            } catch (_: Throwable) {
                null
            }
        }
        if (parsed != null) cache.put(value, parsed)
        return parsed
    }

    private companion object {
        const val CACHE_SIZE = 300
    }
}
