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

package io.getstream.chat.android.ai.openui.lang

import java.net.URI

/** A parsed validation rule, e.g. `required`, `minLength:3`, `pattern:^[a-z]+$` (port of `ParsedRule`). */
internal data class ParsedRule(val type: String, val argument: OpenUIValue? = null)

/** Validates a field value against a rule argument; returns an error message or `null` (port of `ValidatorFn`). */
internal typealias ValidatorFn = (value: OpenUIValue?, argument: OpenUIValue?) -> String?

/** The validators of the built-in rules, by rule type (port of `builtInValidators`). */
internal val builtInValidators: Map<String, ValidatorFn> get() = FormValidation.builtInValidators

/** Parses an array of `"min:8"`-style rule strings; non-strings are ignored (port of `parseRules`). */
internal fun parseRules(rules: OpenUIValue?): List<ParsedRule> = FormValidation.parseRules(rules)

/**
 * Parses a structured rules object (`{required: true, minLength: 5}`); `false` and `null` keys are
 * skipped (port of `parseStructuredRules`).
 */
internal fun parseStructuredRules(rules: OpenUIValue?): List<ParsedRule> = FormValidation.parseStructuredRules(rules)

/**
 * Runs [rules] against [value], stopping at the first error, and returns its message or `null`.
 * [customValidators] take precedence over [builtInValidators] (port of `validate`).
 */
internal fun validate(
    value: OpenUIValue?,
    rules: List<ParsedRule>,
    customValidators: Map<String, ValidatorFn> = emptyMap(),
): String? = FormValidation.validate(value, rules, customValidators)

/** Form field validation (port of `utils/validation.ts`). */
internal object FormValidation {

    private val numericRules = setOf("min", "max", "minLength", "maxLength")

    /** Parses `"min:8"`-style rule strings. */
    fun parseRule(rule: String): ParsedRule {
        val colon = rule.indexOf(':')
        if (colon == -1) return ParsedRule(rule)
        val type = rule.substring(0, colon)
        val raw = rule.substring(colon + 1)
        val number = JS.stringToNumber(raw)
        return ParsedRule(
            type,
            if (type in numericRules && !number.isNaN()) {
                OpenUIValue.Num(number)
            } else {
                OpenUIValue.Str(raw)
            },
        )
    }

    /** Parses an array of rule strings (non-strings are ignored). */
    fun parseRules(rules: OpenUIValue?): List<ParsedRule> =
        rules.arrayValue?.mapNotNull { it.stringValue }?.map(::parseRule) ?: emptyList()

    /** Parses a structured rules object (`{required: true, minLength: 5}`); `false`/`null` keys are skipped. */
    fun parseStructuredRules(rules: OpenUIValue?): List<ParsedRule> =
        rules.objectValue?.mapNotNull { (key, value) ->
            when (value) {
                OpenUIValue.Bool(false), OpenUIValue.Null -> null
                OpenUIValue.Bool(true) -> ParsedRule(key)
                else -> ParsedRule(key, value)
            }
        } ?: emptyList()

    /** Parses either rule format. */
    fun rules(value: OpenUIValue?): List<ParsedRule> =
        if (value is OpenUIValue.Arr) parseRules(value) else parseStructuredRules(value)

    internal fun isEmpty(value: OpenUIValue?): Boolean = when (value) {
        null, OpenUIValue.Null -> true
        is OpenUIValue.Str -> value.value.isEmpty()
        is OpenUIValue.Arr -> value.items.isEmpty()
        // Form state stores { value, componentType }: validate the actual value.
        is OpenUIValue.Obj -> value.fields["value"]?.let(::isEmpty) ?: value.fields.isEmpty()
        else -> false
    }

    private fun number(value: OpenUIValue?): Double = value.numberValue ?: JS.parseFloat(JS.toString(value))

    internal val builtInValidators: Map<String, ValidatorFn> = mapOf(
        "required" to { value, _ ->
            val options = value.objectValue?.values
            when {
                isEmpty(value) -> "This field is required"
                options != null &&
                    options.isNotEmpty() &&
                    options.all { it is OpenUIValue.Bool } &&
                    options.none { it == OpenUIValue.True } ->
                    "At least one option is required"
                else -> null
            }
        },
        "email" to { value, _ ->
            when {
                isEmpty(value) -> null
                value !is OpenUIValue.Str -> "Please enter a valid email"
                Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""").containsMatchIn(value.value) -> null
                else -> "Please enter a valid email"
            }
        },
        // WHATWG `new URL()` is approximated by an absolute java.net.URI.
        "url" to { value, _ ->
            when {
                isEmpty(value) -> null
                value is OpenUIValue.Str && runCatching { URI(value.value) }.getOrNull()?.scheme != null -> null
                else -> "Please enter a valid URL"
            }
        },
        "numeric" to { value, _ ->
            when {
                isEmpty(value) -> null
                value is OpenUIValue.Num && !value.value.isNaN() -> null
                value is OpenUIValue.Str &&
                    !JS
                        .parseFloat(value.value).isNaN() &&
                    JS.trim(value.value).isNotEmpty() -> null
                else -> "Must be a number"
            }
        },
        "min" to { value, argument ->
            val n = number(value)
            val min = JS.toNumber(argument)
            if (isEmpty(value) || n.isNaN() || n >= min) null else "Must be at least ${JS.numberToString(min)}"
        },
        "max" to { value, argument ->
            val n = number(value)
            val max = JS.toNumber(argument)
            if (isEmpty(value) || n.isNaN() || n <= max) null else "Must be no more than ${JS.numberToString(max)}"
        },
        "minLength" to { value, argument ->
            val min = JS.toNumber(argument)
            if (isEmpty(value) || value !is OpenUIValue.Str || value.value.length >= min) {
                null
            } else {
                "Must be at least ${JS.numberToString(min)} characters"
            }
        },
        "maxLength" to { value, argument ->
            val max = JS.toNumber(argument)
            if (isEmpty(value) || value !is OpenUIValue.Str || value.value.length <= max) {
                null
            } else {
                "Must be no more than ${JS.numberToString(max)} characters"
            }
        },
        "pattern" to { value, argument ->
            val pattern = argument.stringValue
            val regex = pattern?.let { runCatching { Regex(it) }.getOrNull() }
            val text = (value as? OpenUIValue.Str)?.value
            when {
                isEmpty(value) || text == null || regex == null -> null
                matchesWithin(regex, text, PATTERN_TIMEOUT_NANOS) -> null
                else -> "Invalid format"
            }
        },
    )

    /**
     * Whether [regex] finds a match in [text] within [timeoutNanos]. The model writes the pattern,
     * so it may backtrack for practically forever (`^(a+)+$` against `aaa…a!`); running out of time
     * counts as no match.
     */
    internal fun matchesWithin(regex: Regex, text: String, timeoutNanos: Long): Boolean = try {
        regex.containsMatchIn(DeadlineCharSequence(text, System.nanoTime() + timeoutNanos))
    } catch (@Suppress("SwallowedException") timeout: MatchTimeoutException) {
        false
    }

    /** Thrown by [DeadlineCharSequence] once its deadline passes, which ends the match. */
    private class MatchTimeoutException : RuntimeException() {
        // Thrown to unwind a match, not to be logged: skip the stack trace.
        override fun fillInStackTrace(): Throwable = this
    }

    /** Text that throws [MatchTimeoutException] when read after [deadline] ([System.nanoTime]). */
    private class DeadlineCharSequence(private val text: CharSequence, private val deadline: Long) : CharSequence {
        private var reads = 0

        override val length: Int get() = text.length

        override fun get(index: Int): Char {
            // Reading the clock on every char would slow the match down; every 1024th read is enough.
            if (++reads and CLOCK_CHECK_MASK == 0 && System.nanoTime() - deadline > 0) throw MatchTimeoutException()
            return text[index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
            DeadlineCharSequence(text.subSequence(startIndex, endIndex), deadline)

        override fun toString(): String = text.toString()
    }

    private const val CLOCK_CHECK_MASK = 1023

    /** How long a `pattern` rule may take to match: 100 ms. */
    private const val PATTERN_TIMEOUT_NANOS = 100_000_000L

    /** Runs rules against a value, stopping at the first error. Custom validators take precedence. */
    fun validate(
        value: OpenUIValue?,
        rules: List<ParsedRule>,
        customValidators: Map<String, ValidatorFn> = emptyMap(),
    ): String? {
        for (rule in rules) {
            val validator = customValidators[rule.type] ?: builtInValidators[rule.type] ?: continue
            validator(value, rule.argument)?.let { return it }
        }
        return null
    }
}
