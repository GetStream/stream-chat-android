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

package io.getstream.chat.android.client.parser2.adapters.internal

import android.os.Build
import androidx.collection.LruCache
import com.ethlo.time.ITU
import io.getstream.chat.android.client.utils.threadLocal
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.log.taggedLogger
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicInteger

/**
 * This class handles parse and format date in the standard way of Stream.
 */
@InternalStreamChatApi
public class StreamDateFormatter(
    private val src: String? = null,
    private val cacheEnabled: Boolean = false,
) {

    private val logger by taggedLogger("StreamDateFormatter")

    private companion object {
        const val STATS = false
        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val DATE_FORMAT_WITHOUT_NANOSECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    }

    private val dateFormat: SimpleDateFormat by threadLocal {
        SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val dateFormatWithoutNanoseconds: SimpleDateFormat by threadLocal {
        SimpleDateFormat(DATE_FORMAT_WITHOUT_NANOSECONDS, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    internal val datePattern = DATE_FORMAT

    private val cache by lazy { LruCache<String, Date>(300) }
    private val cacheHit = AtomicInteger()
    private val allRequests = AtomicInteger()

    /**
     * Parses the [String] to [Date] in the standard way to Stream's API
     */
    internal fun parse(rawValue: String): Date? {
        logCacheHitStats()
        return parseInternal(rawValue)
    }

    private fun logCacheHitStats() {
        val allRequests = allRequests.get()
        val cacheHit = cacheHit.get()
        if (STATS && cacheEnabled && allRequests % 100 == 0) {
            val hitRate = cacheHit.toDouble() / allRequests
            val hitRatePercent = (hitRate * 10000).toInt() / 100f
            logger.v { "[parse] cache hit rate($src): $hitRatePercent% ($cacheHit / $allRequests)" }
        }
    }

    private fun parseInternal(rawValue: String): Date? {
        allRequests.incrementAndGet()
        val cachedValue = fetchFromCache(rawValue)
        if (cachedValue != null) {
            cacheHit.incrementAndGet()
            return cachedValue
        }
        return if (rawValue.isEmpty()) {
            null
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Java Instant is up to 4x faster for parsing
                    // and can parse the date both with and without nano-seconds
                    Date.from(ITU.parseDateTime(rawValue).toInstant())
                } else {
                    dateFormat.parse(rawValue)
                }
            } catch (_: Throwable) {
                try {
                    dateFormatWithoutNanoseconds.parse(rawValue)
                } catch (_: Throwable) {
                    null
                }
            }
        }.also {
            saveToCache(rawValue, it)
        }
    }

    private fun fetchFromCache(rawValue: String): Date? {
        if (!cacheEnabled) return null
        return cache[rawValue]
    }

    private fun saveToCache(rawValue: String, date: Date?) {
        if (!cacheEnabled) return
        if (date == null) return
        cache.put(rawValue, date)
    }

    /**
     * Formats the [Date] in the standard way to Stream's API
     */
    public fun format(date: Date): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ITU.formatUtcMilli(date.toInstant().atOffset(ZoneOffset.UTC))
        } else {
            dateFormat.format(date)
        }
    }
}
