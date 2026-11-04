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

package io.getstream.chat.android.ai.a2ui.core

import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/** The A2UI 1.0 basic catalog: the published catalog document, with its functions implemented. */
internal object BasicCatalog {
    /** The basic catalog. */
    val catalog: Catalog by lazy {
        val document = A2UIJson.map(A2UIJson.parse(Resources.read("basic_catalog.json"))).orEmpty()
        Catalog.fromJson(document, BasicFunctions.implementations)
    }
}

/** What the basic catalog's functions do. */
@Suppress("TooManyFunctions")
internal object BasicFunctions {

    private const val MAX_DECIMALS = 15
    private const val DEFAULT_MAX_DECIMALS = 3

    /** The implementation of each function of the basic catalog, by name. */
    val implementations: Map<String, FunctionImplementation> = mapOf(
        "required" to ::required,
        "regex" to ::regex,
        "length" to ::length,
        "numeric" to ::numeric,
        "email" to ::email,
        "formatString" to { it.interpolate(it.requireString("value")) },
        "formatNumber" to ::formatNumber,
        "formatCurrency" to ::formatCurrency,
        "formatDate" to ::formatDate,
        "pluralize" to ::pluralize,
        "openUrl" to ::openUrl,
        "and" to { invocation -> values(invocation).all(::truthy) },
        "or" to { invocation -> values(invocation).any(::truthy) },
        "not" to { invocation -> !truthy(invocation.value("value")) },
    )

    private const val INVALID_EMAIL_MESSAGE = "Please enter a valid email address (e.g. user@example.com)."
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /** The message of a failed basic check when neither the check nor its function gives one. */
    fun defaultMessage(function: String): String? = when (function) {
        "required" -> "This field is required."
        "regex" -> "This value has an invalid format."
        "length" -> "This value has an invalid length."
        "numeric" -> "Enter a number in the allowed range."
        else -> null
    }

    /**
     * Whether [value] counts as true: a validation result by its `valid` flag, so `and`, `or` and
     * `not` combine checks; otherwise as JavaScript does.
     */
    fun truthy(value: Any?): Boolean {
        if (value == null) return false
        A2UIJson.map(value)?.let { map -> map["valid"]?.let(A2UIJson::bool)?.let { return it } }
        return when (value) {
            is Boolean -> value
            is Number -> value.toDouble().let { it != 0.0 && !it.isNaN() }
            is String -> value.isNotEmpty()
            else -> true
        }
    }

    private fun result(valid: Boolean): Map<String, Any?> = ValidationResult(valid).toJson()

    private fun required(invocation: FunctionInvocation): Any = result(
        when (val value = invocation.value("value")) {
            null -> false
            is String -> value.isNotEmpty()
            is List<*> -> value.isNotEmpty()
            else -> true
        },
    )

    // An empty value passes, so an optional field shows no error before the person types.
    private fun regex(invocation: FunctionInvocation): Any {
        val pattern = invocation.requireString("pattern")
        val value = invocation.string("value").orEmpty()
        val regex = A2UIRegex.compile(pattern)
        if (value.isEmpty()) return result(true)
        if (value.codePointCount() > A2UIRegex.MAX_VALUE_LENGTH) return result(false)
        return result(A2UIRegex.containsMatch(regex, value))
    }

    private fun length(invocation: FunctionInvocation): Any {
        val count = invocation.list("value")?.size ?: invocation.string("value").orEmpty().codePointCount()
        invocation.int("min")?.let { if (count < it) return result(false) }
        invocation.int("max")?.let { if (count > it) return result(false) }
        return result(true)
    }

    private fun numeric(invocation: FunctionInvocation): Any {
        val raw = invocation.value("value") ?: return result(true)
        val value = A2UIJson.double(raw) ?: return result(false)
        invocation.double("min")?.let { if (value < it) return result(false) }
        invocation.double("max")?.let { if (value > it) return result(false) }
        return result(true)
    }

    private fun email(invocation: FunctionInvocation): Any {
        val value = invocation.string("value").orEmpty()
        if (value.isEmpty() || EMAIL.matches(value)) return result(true)
        return ValidationResult(false, code = "INVALID_EMAIL", message = INVALID_EMAIL_MESSAGE).toJson()
    }

    private fun formatNumber(invocation: FunctionInvocation): Any {
        val value = invocation.requireDouble("value")
        val format = NumberFormat.getNumberInstance(invocation.services.locale)
        format.isGroupingUsed = invocation.bool("grouping") ?: true
        format.roundingMode = RoundingMode.HALF_UP
        val decimals = invocation.int("decimals")?.coerceIn(0, MAX_DECIMALS)
        format.minimumFractionDigits = decimals ?: 0
        format.maximumFractionDigits = decimals ?: DEFAULT_MAX_DECIMALS
        return format.format(value)
    }

    private fun formatCurrency(invocation: FunctionInvocation): Any {
        val value = invocation.requireDouble("value")
        val code = invocation.requireString("currency")
        val currency = try {
            Currency.getInstance(code.uppercase(Locale.ROOT))
        } catch (_: IllegalArgumentException) {
            return "$code ${A2UIJson.stringify(value)}"
        }
        val format = NumberFormat.getCurrencyInstance(invocation.services.locale)
        format.currency = currency
        format.isGroupingUsed = invocation.bool("grouping") ?: true
        format.roundingMode = RoundingMode.HALF_UP
        invocation.int("decimals")?.coerceIn(0, MAX_DECIMALS)?.let { decimals ->
            format.minimumFractionDigits = decimals
            format.maximumFractionDigits = decimals
        }
        return format.format(value)
    }

    // A written date keeps the zone it was written in, so a payload shows the same time on every
    // device; a date without a zone is a wall-clock time. Only epoch timestamps use the device's.
    private fun formatDate(invocation: FunctionInvocation): Any {
        val pattern = invocation.requireString("format")
        val raw = invocation.value("value") ?: throw FunctionException.missingArgument("value", "formatDate")
        val date = A2UIDates.parse(raw, invocation.services.timeZone) ?: return ""
        if (pattern == "ISO") return A2UIDates.iso(date)
        val formatter = try {
            SimpleDateFormat(pattern, invocation.services.locale)
        } catch (_: IllegalArgumentException) {
            throw FunctionException.invalidArgument("format", "formatDate", "'$pattern' is not a valid date pattern.")
        }
        formatter.timeZone = date.timeZone
        return formatter.format(Date(date.millis))
    }

    // The CLDR categories of the platform aren't available on every supported version, so the
    // explicit `zero`, `one` and `two` forms are picked, and `other` stands in for the rest.
    private fun pluralize(invocation: FunctionInvocation): Any {
        val value = invocation.requireDouble("value")
        val other = invocation.requireString("other")
        val magnitude = abs(value)
        if (value == 0.0) invocation.string("zero")?.let { return it }
        if (magnitude == 1.0) invocation.string("one")?.let { return it }
        if (magnitude == 2.0) invocation.string("two")?.let { return it }
        return other
    }

    private fun openUrl(invocation: FunctionInvocation): Any? {
        val url = invocation.requireString("url")
        if (!invocation.services.canOpenUrl(url)) {
            throw FunctionException(RendererError.EXPRESSION_ERROR, "Function 'openUrl' may not open '$url'.")
        }
        if (!invocation.services.openUrl(url)) {
            throw FunctionException(RendererError.EXECUTION_ERROR, "Function 'openUrl' could not open '$url'.")
        }
        return null
    }

    private fun values(invocation: FunctionInvocation): List<Any?> {
        val values = invocation.list("values")
        if (values == null || values.size < 2) {
            throw FunctionException(
                RendererError.EXPRESSION_ERROR,
                "Function '${invocation.name}' needs at least two values.",
            )
        }
        return values
    }

    private fun String.codePointCount(): Int = codePointCount(0, length)
}

/** Reads the dates A2UI payloads carry, without `java.time`, which the minimum SDK lacks. */
internal object A2UIDates {
    /** A moment and the zone it is shown in. */
    data class ZonedMillis(val millis: Long, val timeZone: TimeZone)

    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    private val DATE_TIME = Regex(
        "^(\\d{4})-(\\d{2})-(\\d{2})" +
            "(?:[T ](\\d{2}):(\\d{2})(?::(\\d{2})(?:\\.(\\d{1,9}))?)?)?" +
            "(Z|z|[+-]\\d{2}:?\\d{2})?$",
    )
    private val TIME = Regex("^(\\d{2}):(\\d{2})(?::(\\d{2}))?$")
    private const val MILLIS_THRESHOLD = 100_000_000_000.0
    private const val MILLIS_PER_SECOND = 1000
    private const val MINUTES_PER_HOUR = 60
    private const val MILLIS_PER_MINUTE = 60_000
    private const val MAX_HOUR = 23
    private const val MAX_MINUTE = 59
    private const val FRACTION_DIGITS = 3
    private const val EPOCH_YEAR = 1970
    private const val GROUP_FRACTION = 7
    private const val GROUP_OFFSET = 8

    /**
     * Reads a date: ISO 8601 date-times, dates and times, or epoch timestamps in seconds or
     * milliseconds (in [deviceZone]). Returns `null` for a date that doesn't exist, such as
     * February 30.
     */
    fun parse(value: Any?, deviceZone: TimeZone): ZonedMillis? {
        if (value == null) return null
        if (value !is String) {
            val number = A2UIJson.double(value) ?: return null
            val millis = if (abs(number) > MILLIS_THRESHOLD) number else number * MILLIS_PER_SECOND
            return ZonedMillis(millis.toLong(), deviceZone)
        }
        val text = value.trim()
        DATE_TIME.matchEntire(text)?.let { match -> return dateTime(match.groupValues) }
        TIME.matchEntire(text)?.let { match ->
            val (hour, minute, second) = match.destructured
            return calendar(UTC, EPOCH_YEAR, 1, 1, hour.toInt(), minute.toInt(), second.ifEmpty { "0" }.toInt(), 0)
                ?.let { ZonedMillis(it, UTC) }
        }
        return null
    }

    /** [date] as ISO 8601 with milliseconds, in its zone. */
    fun iso(date: ZonedMillis): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT)
        formatter.timeZone = date.timeZone
        val offsetMinutes = date.timeZone.getOffset(date.millis) / MILLIS_PER_MINUTE
        val offset = if (offsetMinutes == 0) {
            "Z"
        } else {
            val sign = if (offsetMinutes < 0) "-" else "+"
            val minutes = abs(offsetMinutes)
            "%s%02d:%02d".format(Locale.ROOT, sign, minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR)
        }
        return formatter.format(Date(date.millis)) + offset
    }

    private fun dateTime(groups: List<String>): ZonedMillis? {
        val zone = zone(groups[GROUP_OFFSET]) ?: return null
        val fraction = groups[GROUP_FRACTION].padEnd(FRACTION_DIGITS, '0').take(FRACTION_DIGITS).ifEmpty { "0" }.toInt()
        val millis = calendar(
            zone = zone,
            year = groups[1].toInt(),
            month = groups[2].toInt(),
            day = groups[3].toInt(),
            hour = groups[4].ifEmpty { "0" }.toInt(),
            minute = groups[5].ifEmpty { "0" }.toInt(),
            second = groups[6].ifEmpty { "0" }.toInt(),
            millisecond = fraction,
        ) ?: return null
        return ZonedMillis(millis, zone)
    }

    // `null` for an offset no clock shows, such as +24:00 or +05:60.
    private fun zone(offset: String): TimeZone? {
        if (offset.isEmpty() || offset == "Z" || offset == "z") return UTC
        val digits = offset.substring(1).replace(":", "")
        val offsetHours = digits.take(2).toInt()
        val offsetMinutes = digits.drop(2).toInt()
        if (offsetHours > MAX_HOUR || offsetMinutes > MAX_MINUTE) return null
        val minutes = offsetHours * MINUTES_PER_HOUR + offsetMinutes
        val sign = if (offset.startsWith("-")) -1 else 1
        val signText = if (sign < 0) "-" else "+"
        val name = "GMT%s%02d:%02d".format(
            Locale.ROOT,
            signText,
            minutes / MINUTES_PER_HOUR,
            minutes % MINUTES_PER_HOUR,
        )
        return TimeZone.getTimeZone(name)
    }

    @Suppress("LongParameterList")
    private fun calendar(
        zone: TimeZone,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
        millisecond: Int,
    ): Long? {
        if (hour > MAX_HOUR || minute > MAX_MINUTE || second > MAX_MINUTE) return null
        val calendar = GregorianCalendar(zone)
        calendar.isLenient = false
        calendar.clear()
        calendar.set(year, month - 1, day, hour, minute, second)
        calendar.set(Calendar.MILLISECOND, millisecond)
        return try {
            calendar.timeInMillis
        } catch (_: IllegalArgumentException) {
            // A day that doesn't exist, such as February 30.
            null
        }
    }
}
