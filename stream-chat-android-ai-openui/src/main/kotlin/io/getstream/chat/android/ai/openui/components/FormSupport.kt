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

package io.getstream.chat.android.ai.openui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.lang.FormValidation
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.ParsedRule
import io.getstream.chat.android.ai.openui.lang.stringValue
import io.getstream.chat.android.ai.openui.utils.NumberFormatting
import kotlin.math.abs
import kotlin.math.round

/**
 * A field's first broken rule, and the message the language runtime gives for it, which shows when
 * the rule isn't one of the built-in ones that have a translated text.
 */
internal data class FieldError(val rule: ParsedRule, val message: String) {
    companion object {
        /** The first rule of [rules] that [value] breaks, or `null` when it keeps them all. */
        fun of(value: OpenUIValue?, rules: List<ParsedRule>): FieldError? = rules.firstNotNullOfOrNull { rule ->
            FormValidation.validate(value, listOf(rule))?.let { FieldError(rule, it) }
        }
    }
}

/** The error as people read it, in the app's language. */
@Composable
internal fun FieldError.text(): String {
    val limit = rule.argument?.let { NumberFormatting.decimal(JS.toNumber(it)) }.orEmpty()
    return when (rule.type) {
        // A group of checkboxes that has none checked reads differently from an empty field.
        "required" -> if (message == FormValidation.validate(OpenUIValue.Null, listOf(rule))) {
            stringResource(R.string.stream_ai_openui_validation_required)
        } else {
            stringResource(R.string.stream_ai_openui_validation_required_option)
        }
        "email" -> stringResource(R.string.stream_ai_openui_validation_email)
        "url" -> stringResource(R.string.stream_ai_openui_validation_url)
        "numeric" -> stringResource(R.string.stream_ai_openui_validation_numeric)
        "min" -> stringResource(R.string.stream_ai_openui_validation_min, limit)
        "max" -> stringResource(R.string.stream_ai_openui_validation_max, limit)
        "minLength" -> stringResource(R.string.stream_ai_openui_validation_min_length, limit)
        "maxLength" -> stringResource(R.string.stream_ai_openui_validation_max_length, limit)
        "pattern" -> stringResource(R.string.stream_ai_openui_validation_pattern)
        else -> message
    }
}

/**
 * Per-form validation state (port of `useCreateFormValidation`). `Form` creates one and shares it
 * with its fields; errors are snapshot state, so fields recompose when they change.
 */
@Stable
internal class FormValidationModel {
    private val errors = mutableStateMapOf<String, FieldError>()

    private class Registration(val rules: List<ParsedRule>, val value: () -> OpenUIValue?)

    private val fields = LinkedHashMap<String, Registration>()

    fun error(name: String): FieldError? = errors[name]

    val hasErrors: Boolean get() = errors.isNotEmpty()

    fun register(name: String, rules: List<ParsedRule>, value: () -> OpenUIValue?) {
        fields[name] = Registration(rules, value)
    }

    fun unregister(name: String) {
        fields.remove(name)
    }

    /** Validates one field and records its error; `true` when valid. */
    fun validateField(name: String, value: OpenUIValue?, rules: List<ParsedRule>): Boolean {
        val error = FieldError.of(unwrap(value), rules)
        if (error == null) errors.remove(name) else errors[name] = error
        return error == null
    }

    /** Validates every registered field; `true` when the form is valid. */
    fun validateForm(): Boolean {
        val next = fields
            .mapNotNull { (name, registration) ->
                FieldError.of(unwrap(registration.value()), registration.rules)?.let { name to it }
            }.toMap()
        errors.keys.retainAll(next.keys)
        errors.putAll(next)
        return next.isEmpty()
    }

    fun clearError(name: String) {
        errors.remove(name)
    }

    private fun unwrap(value: OpenUIValue?): OpenUIValue? {
        val fields = (value as? OpenUIValue.Obj)?.fields ?: return value
        return if ("value" in fields && "componentType" in fields) fields["value"] else value
    }
}

/** The enclosing `Form`'s validation model. */
internal val LocalFormValidation = compositionLocalOf<FormValidationModel?> { null }

/** Registers a field's rules with the enclosing form while it is shown and the response isn't streaming. */
@Composable
internal fun RegisterValidation(
    validation: FormValidationModel?,
    name: String,
    rules: List<ParsedRule>,
    isStreaming: Boolean,
    value: () -> OpenUIValue?,
) {
    val currentValue = rememberUpdatedState(value)
    LaunchedEffect(validation, name, rules, isStreaming) {
        if (validation != null && rules.isNotEmpty() && !isStreaming) {
            validation.register(name, rules) { currentValue.value() }
        }
    }
    DisposableEffect(validation, name) {
        onDispose { validation?.unregister(name) }
    }
}

/** Helpers for selection fields (port of `selectionFieldUtils.ts`). */
internal object SelectionField {
    /** The stored value (or else the default) as the selected values. */
    fun normalize(single: Boolean, stored: OpenUIValue?, defaultValue: OpenUIValue?): List<String> {
        fun first(value: OpenUIValue?): List<String>? = when (value) {
            is OpenUIValue.Str -> if (value.value.isEmpty()) emptyList() else listOf(value.value)
            is OpenUIValue.Arr -> value.items.firstOrNull()?.stringValue?.let {
                if (it.isEmpty()) {
                    emptyList()
                } else {
                    listOf(it)
                }
            }
            else -> null
        }
        fun all(value: OpenUIValue?): List<String>? = when (value) {
            is OpenUIValue.Arr -> value.items.mapNotNull { it.stringValue }
            is OpenUIValue.Str -> if (value.value.isEmpty()) emptyList() else listOf(value.value)
            else -> null
        }
        return if (single) {
            first(stored) ?: first(defaultValue) ?: emptyList()
        } else {
            all(stored) ?: all(defaultValue)
                ?: emptyList()
        }
    }

    /** The default shaped for storage: a string for single selection, an array for multiple. */
    fun storedDefault(single: Boolean, defaultValue: OpenUIValue?): OpenUIValue? {
        if (single) {
            return when (defaultValue) {
                is OpenUIValue.Str -> defaultValue.takeIf { it.value.isNotEmpty() }
                is OpenUIValue.Arr -> defaultValue.items.firstOrNull()?.stringValue?.takeIf { it.isNotEmpty() }?.let(
                    OpenUIValue::Str,
                )
                else -> null
            }
        }
        return when (defaultValue) {
            is OpenUIValue.Arr -> OpenUIValue.Arr(defaultValue.items.filter { it.stringValue != null })
            is OpenUIValue.Str -> OpenUIValue.Arr(
                if (defaultValue.value.isEmpty()) emptyList() else listOf(defaultValue),
            )
            else -> null
        }
    }

    fun toggle(single: Boolean, selection: List<String>, value: String): List<String> = when {
        single -> if (selection.firstOrNull() == value) emptyList() else listOf(value)
        value in selection -> selection - value
        else -> selection + value
    }
}

/** A slider's bounds, step and values, kept within what the model can make it draw. */
internal object SliderValues {
    private const val DEFAULT_MAX = 100.0
    private const val ROUNDING = 1000.0
    private const val MAX_TICKS = 200

    // Beyond this a Float can't tell a slider's positions apart.
    private const val LARGEST_BOUND = 1e15

    /** `min` to `max`, falling back to `0` and `100` when not finite, and at least one apart. */
    fun bounds(minimum: Double?, maximum: Double?): ClosedFloatingPointRange<Double> {
        val lower = minimum?.takeIf { it.isFinite() && abs(it) <= LARGEST_BOUND } ?: 0.0
        val upper = maximum?.takeIf { it.isFinite() && abs(it) <= LARGEST_BOUND } ?: DEFAULT_MAX
        return lower..maxOf(upper, lower + 1)
    }

    /** The step: a positive finite one within the bounds, `1` for a discrete slider, or none. */
    fun step(step: Double?, variant: String?, bounds: ClosedFloatingPointRange<Double>): Double? =
        step?.takeIf { it.isFinite() && it > 0 && it <= bounds.endInclusive - bounds.start }
            ?: if (variant == "discrete") 1.0 else null

    /** The values of a stored value or default: one or two numbers, within the bounds. */
    fun values(value: OpenUIValue?, bounds: ClosedFloatingPointRange<Double>): List<Double> {
        val numbers = when (value) {
            is OpenUIValue.Num -> listOf(value.value)
            is OpenUIValue.Str -> listOf(JS.toNumber(value))
            is OpenUIValue.Arr -> value.items.take(2).map(JS::toNumber)
            else -> emptyList()
        }.filter { it.isFinite() }
        return if (numbers.isEmpty()) listOf(bounds.start) else clamped(numbers, bounds)
    }

    /** At most two values, within the bounds, the lower one first. */
    fun clamped(values: List<Double>, bounds: ClosedFloatingPointRange<Double>): List<Double> {
        val inBounds = values.take(2).map { if (it.isNaN()) bounds.start else it.coerceIn(bounds) }
        return if (inBounds.size == 2) inBounds.sorted() else inBounds
    }

    /** [value] on the nearest step, within the bounds. */
    fun snapped(value: Double, bounds: ClosedFloatingPointRange<Double>, step: Double?): Double {
        val onStep = step?.let { bounds.start + round((value - bounds.start) / it) * it } ?: value
        return onStep.coerceIn(bounds)
    }

    /** The ticks between the ends Compose snaps to, or none when there would be too many to see. */
    fun tickCount(bounds: ClosedFloatingPointRange<Double>, step: Double?): Int {
        val count = step?.let { round((bounds.endInclusive - bounds.start) / it) - 1 } ?: return 0
        return if (count in 0.0..MAX_TICKS.toDouble()) count.toInt() else 0
    }

    /** The values as the slider stores them, rounded to three decimals. */
    fun stored(values: List<Double>): OpenUIValue =
        OpenUIValue.Arr(values.map { OpenUIValue.Num(round(it * ROUNDING) / ROUNDING) })
}
