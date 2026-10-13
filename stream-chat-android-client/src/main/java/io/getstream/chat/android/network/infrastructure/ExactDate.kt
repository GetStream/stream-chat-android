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

import android.os.Build
import com.ethlo.time.ITU
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * A date-time as a [Date] together with the string the server sent for it.
 * [raw] keeps the full precision (up to nanoseconds) that [Date] cannot hold,
 * and is what gets written back when the value is serialized.
 * A date sent as unix nanoseconds keeps [raw] as the same instant in RFC 3339 with 9 fractional digits.
 */
internal class ExactDate private constructor(
    private val epochMillis: Long,
    internal val raw: String,
) {

    /** The instant as a [Date], truncated to milliseconds. A new instance on each call, as [Date] is mutable. */
    internal val date: Date get() = Date(epochMillis)

    override fun equals(other: Any?): Boolean =
        other is ExactDate && epochMillis == other.epochMillis && raw == other.raw

    override fun hashCode(): Int = 31 * epochMillis.hashCode() + raw.hashCode()

    override fun toString(): String = raw

    internal companion object {

        // Parses and formats on API < 26, where ITU's java.time types aren't available.
        private val withMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        // Formats the seconds of a date sent as unix nanoseconds.
        private val toSeconds = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        // Guards withMillis and toSeconds, which are not thread-safe.
        private val legacyFormatterLock = Any()

        /**
         * Parses an RFC 3339 date-time, keeping [raw] as given, or unix nanoseconds.
         * On API 26+ any offset and up to 9 fractional digits are accepted; below API 26 only a `Z`
         * offset, with the fraction read to milliseconds.
         * Returns null if [raw] is empty or cannot be parsed.
         */
        internal fun parseOrNull(raw: String): ExactDate? {
            if (raw.isEmpty()) return null
            if (raw.all { it in '0'..'9' }) return fromUnixNanosOrNull(raw)
            val parsed = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Date.from(ITU.parseDateTime(raw).toInstant())
                } else {
                    synchronized(legacyFormatterLock) { withMillis.parse(toMillisFraction(raw)) }
                }
            } catch (_: Throwable) {
                null
            }
            return parsed?.let { ExactDate(it.time, raw) }
        }

        /**
         * Rewrites `yyyy-MM-ddTHH:mm:ss[.fraction]Z` with exactly 3 fractional digits, truncating or padding,
         * so the result does not depend on how SimpleDateFormat reads other lengths: Android reads the
         * digits as a fraction, the desktop JVM as a count of milliseconds. Other input is returned
         * unchanged.
         */
        private fun toMillisFraction(raw: String): String {
            if (raw.length < SECONDS_END + 1 || raw.last() != 'Z') return raw
            val seconds = raw.substring(0, SECONDS_END)
            if (raw.length == SECONDS_END + 1) return "$seconds.000Z"
            if (raw[SECONDS_END] != '.') return raw
            val fraction = raw.substring(SECONDS_END + 1, raw.length - 1)
            return "$seconds.${fraction.take(3).padEnd(3, '0')}Z"
        }

        private fun fromUnixNanosOrNull(raw: String): ExactDate? {
            val nanos = raw.toLongOrNull() ?: return null
            val epochMillis = nanos / NANOS_PER_MILLI
            val seconds = synchronized(legacyFormatterLock) { toSeconds.format(Date(epochMillis)) }
            val fraction = (nanos % NANOS_PER_SECOND).toString().padStart(NANO_DIGITS, '0')
            return ExactDate(epochMillis, "$seconds.${fraction}Z")
        }

        private const val NANOS_PER_SECOND = 1_000_000_000L
        private const val NANOS_PER_MILLI = 1_000_000L
        private const val NANO_DIGITS = 9

        // Length of `yyyy-MM-ddTHH:mm:ss`.
        private const val SECONDS_END = 19

        /** Wraps a client-side [date]; [raw] is its ISO-8601 UTC form with milliseconds. */
        internal fun fromDate(date: Date): ExactDate {
            val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ITU.formatUtcMilli(date.toInstant().atOffset(ZoneOffset.UTC))
            } else {
                synchronized(legacyFormatterLock) { withMillis.format(date) }
            }
            return ExactDate(date.time, raw)
        }
    }
}
