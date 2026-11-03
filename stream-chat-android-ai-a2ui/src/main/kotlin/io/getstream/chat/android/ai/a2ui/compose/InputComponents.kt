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

package io.getstream.chat.android.ai.a2ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.a2ui.R
import io.getstream.chat.android.ai.a2ui.core.A2UIJson
import kotlin.math.roundToInt

/**
 * `Button`: its child in a `primary`, `borderless` or default button. A tap does what its action
 * says. While one of its checks fails, it is disabled and says why.
 */
@Composable
internal fun A2UIButton(scope: A2UIComponentScope) {
    val focusManager = LocalFocusManager.current
    val openModal = LocalModalOpener.current
    val errors = scope.validationErrors
    val onClick: () -> Unit = {
        // Closes the keyboard of a text field, so the answer to the action shows.
        focusManager.clearFocus()
        openModal?.invoke()
        scope.performAction()
    }
    val content: @Composable () -> Unit = {
        // A trigger's own button opens the modal; the content doesn't open it again.
        CompositionLocalProvider(LocalModalOpener provides null) {
            scope.child()?.let { scope.Child(it) }
        }
    }
    Column(modifier = scope.modifier.padding(LeafMargin)) {
        val enabled = errors.isEmpty()
        when (scope.hint("variant")) {
            "primary" -> Button(onClick = onClick, enabled = enabled) { content() }
            "borderless" -> TextButton(onClick = onClick, enabled = enabled) { content() }
            else -> OutlinedButton(onClick = onClick, enabled = enabled) { content() }
        }
        ValidationMessages(errors)
    }
}

/**
 * `TextField`: a `shortText`, `longText`, `number` or `obscured` field. Its `value` is a two-way
 * binding: every change goes to the data model. A failing check marks it and says why.
 */
@Composable
internal fun A2UITextField(scope: A2UIComponentScope) {
    val value = scope.string("value").orEmpty()
    val variant = scope.hint("variant")
    val errors = scope.validationErrors
    // While the person types, the field is the source of truth; a change from elsewhere, such as
    // the agent's, replaces the text when the field doesn't have focus.
    var field by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value, focused) {
        if (!focused && field.text != value) field = TextFieldValue(value, TextRange(value.length))
    }
    OutlinedTextField(
        value = field,
        onValueChange = { newValue ->
            val changed = newValue.text != field.text
            field = newValue
            if (changed) scope.write("value", newValue.text)
        },
        modifier = scope.modifier
            .fillMaxWidth()
            .padding(LeafMargin)
            .onFocusChanged { focused = it.isFocused },
        label = scope.string("label")?.let { label -> { Text(label) } },
        placeholder = scope.string("placeholder")?.let { placeholder -> { Text(placeholder) } },
        isError = errors.isNotEmpty() && field.text.isNotEmpty(),
        supportingText = errors.firstOrNull()?.takeIf { field.text.isNotEmpty() }?.let { error -> { Text(error) } },
        singleLine = variant != "longText",
        minLines = if (variant == "longText") LongTextLines else 1,
        visualTransformation = if (variant == "obscured") PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = when (variant) {
                "number" -> KeyboardType.Decimal
                "obscured" -> KeyboardType.Password
                else -> KeyboardType.Text
            },
        ),
    )
}

/** `CheckBox`: a checkbox with its label; `value` is a two-way binding. */
@Composable
internal fun A2UICheckBox(scope: A2UIComponentScope) {
    val checked = scope.boolean("value") ?: false
    Column(modifier = scope.modifier.padding(LeafMargin)) {
        Row(
            modifier = Modifier.toggleable(value = checked, role = Role.Checkbox) { scope.write("value", it) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = checked, onCheckedChange = null)
            scope.string("label")?.let { Text(it, modifier = Modifier.padding(start = 8.dp)) }
        }
        ValidationMessages(scope.validationErrors)
    }
}

/**
 * `ChoicePicker`: options to pick one of (`mutuallyExclusive`) or several of
 * (`multipleSelection`), as a list of checkboxes or radio buttons, or as `chips`. With
 * `filterable`, a search field narrows the options. `value` is a two-way binding to the list of
 * picked values.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun A2UIChoicePicker(scope: A2UIComponentScope) {
    val selected = scope.stringList("value").orEmpty()
    val multiple = scope.hint("variant") == "multipleSelection"
    val options = choiceOptions(scope)
    var query by rememberSaveable { mutableStateOf("") }
    val shown = if (query.isBlank()) options else options.filter { it.first.contains(query.trim(), ignoreCase = true) }
    fun toggle(value: String) {
        val next = when {
            multiple && value in selected -> selected - value
            multiple -> selected + value
            else -> listOf(value)
        }
        scope.write("value", next)
    }
    Column(modifier = scope.modifier.fillMaxWidth().padding(LeafMargin)) {
        scope.string("label")?.let { Text(it, style = labelStyle()) }
        if (scope.boolean("filterable") == true) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.stream_ai_a2ui_filter_options)) },
            )
        }
        if (scope.hint("displayStyle") == "chips") {
            FlowRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                shown.forEach { (label, value) ->
                    FilterChip(selected = value in selected, onClick = { toggle(value) }, label = { Text(label) })
                }
            }
        } else {
            shown.forEach { (label, value) ->
                val isSelected = value in selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            role = if (multiple) Role.Checkbox else Role.RadioButton,
                        ) { toggle(value) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (multiple) {
                        Checkbox(checked = isSelected, onCheckedChange = null)
                    } else {
                        RadioButton(selected = isSelected, onClick = null)
                    }
                    Text(label, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        ValidationMessages(scope.validationErrors)
    }
}

// The label and value of each option, with the labels resolved.
private fun choiceOptions(scope: A2UIComponentScope): List<Pair<String, String>> =
    A2UIJson.list(scope.raw("options")).orEmpty().mapNotNull { raw ->
        val option = A2UIJson.map(raw) ?: return@mapNotNull null
        val value = option["value"] as? String ?: return@mapNotNull null
        val label = A2UIJson.string(scope.resolve(option["label"]))
        (label ?: value) to value
    }

/**
 * `Slider`: a number from `min` to `max`, snapping to `steps` divisions when it has them. `value`
 * is a two-way binding.
 */
@Composable
internal fun A2UISlider(scope: A2UIComponentScope) {
    val min = scope.number("min") ?: 0.0
    val max = (scope.number("max") ?: return).coerceAtLeast(min)
    val value = (scope.number("value") ?: min).coerceIn(min, max)
    val divisions = scope.number("steps")?.roundToInt()?.takeIf { it >= 1 }
    Column(modifier = scope.modifier.fillMaxWidth().padding(LeafMargin)) {
        scope.string("label")?.let { label -> Text("$label: ${A2UIJson.stringify(value)}", style = labelStyle()) }
        Slider(
            value = value.toFloat(),
            onValueChange = { picked ->
                val snapped = divisions?.let { snap(picked.toDouble(), min, max, it) } ?: picked.toDouble()
                scope.write("value", snapped)
            },
            valueRange = min.toFloat()..max.toFloat(),
            steps = divisions?.let { (it - 1).coerceAtLeast(0) } ?: 0,
        )
        ValidationMessages(scope.validationErrors)
    }
}

// The value of the division nearest to [value].
private fun snap(value: Double, min: Double, max: Double, divisions: Int): Double {
    if (max <= min) return min
    val step = (max - min) / divisions
    return min + ((value - min) / step).roundToInt() * step
}

private const val LongTextLines = 3
