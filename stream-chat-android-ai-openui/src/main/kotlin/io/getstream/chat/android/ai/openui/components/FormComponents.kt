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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.icons.ErrorOutline
import io.getstream.chat.android.ai.openui.icons.UnfoldMore
import io.getstream.chat.android.ai.openui.lang.ElementNode
import io.getstream.chat.android.ai.openui.lang.FormValidation
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.StateField
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.boolValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue
import io.getstream.chat.android.ai.openui.rendering.ComponentProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer
import io.getstream.chat.android.ai.openui.rendering.LocalOpenUIFormName
import io.getstream.chat.android.ai.openui.rendering.LocalOpenUIStoreSnapshot
import io.getstream.chat.android.ai.openui.rendering.OpenUIIcon
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.rendering.inlineMarkdown
import io.getstream.chat.android.ai.openui.utils.NumberFormatting
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal val formRenderers: Map<String, ComponentRenderer> = mapOf(
    "Form" to { FormComponent(it) },
    "FormControl" to { FormControlComponent(it) },
    "Input" to { TextInputComponent(it, multiline = false) },
    "TextArea" to { TextInputComponent(it, multiline = true) },
    "Select" to { SelectComponent(it) },
    "DatePicker" to { DatePickerComponent(it) },
    "Slider" to { SliderComponent(it) },
    "CheckBoxGroup" to { ToggleGroupComponent(it, isSwitch = false) },
    "SwitchGroup" to { ToggleGroupComponent(it, isSwitch = true) },
    "RadioGroup" to { RadioGroupComponent(it) },
    "Chips" to { SelectionFieldComponent(it, isCards = false) },
    "OptionCards" to { SelectionFieldComponent(it, isCards = true) },
)

// Form & control ---------------------------------------------------------------------------------

/** `Form(name, buttons, fields?)`: scopes field state and validation. */
@Composable
internal fun FormComponent(ctx: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val validation = remember { FormValidationModel() }
    CompositionLocalProvider(
        LocalOpenUIFormName provides ctx.props.text("name"),
        LocalFormValidation provides validation,
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.l)) {
            ctx.RenderProp("fields")
            ctx.RenderProp("buttons")
        }
    }
}

/** `FormControl(label, input, hint?)`: label, field, and the hint or the validation error. */
@Composable
internal fun FormControlComponent(ctx: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val validation = LocalFormValidation.current
    val input = ctx.props.element("input")
    val fieldName = input?.props?.get("name").stringValue
    val isRequired = input?.props?.get("rules")["required"] == OpenUIValue.True
    val error = fieldName?.let { validation?.error(it) }
    val labelStyle = theme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(ctx.props.text("label").orEmpty(), style = labelStyle, color = theme.colors.textPrimary)
            if (isRequired) Text("*", style = labelStyle, color = theme.colors.danger)
        }
        ctx.RenderProp("input")
        val hint = ctx.props.text("hint")
        if (error != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = theme.colors.danger, modifier = Modifier.size(14.dp))
                Text(error.text(), style = theme.typography.caption, color = theme.colors.danger)
            }
        } else if (!hint.isNullOrEmpty()) {
            Text(hint, style = theme.typography.caption, color = theme.colors.textSecondary)
        }
    }
}

/** Text field chrome shared by inputs: sunken background, accent border when focused, red on error. */
@Composable
private fun Modifier.formFieldChrome(hasError: Boolean, isFocused: Boolean): Modifier {
    val theme = OpenUITheme.current
    val shape = RoundedCornerShape(theme.radius.small)
    val stroke = when {
        hasError -> theme.colors.danger
        isFocused -> theme.colors.accent
        else -> Color.Transparent
    }
    return this
        .background(theme.colors.sunkenSurface, shape)
        .border(1.5.dp, stroke, shape)
        .padding(horizontal = theme.spacing.m, vertical = 10.dp)
}

/** The field's value as display text (`""` when unset). */
private fun StateField.text(): String = value.takeUnless(JS::isNullish)?.let(JS::toString).orEmpty()

/** A text area's height in lines: the model's `rows`, kept between 2 and 100, or 3. */
internal fun textAreaRows(rows: Double?): Int =
    rows?.takeIf { it.isFinite() }?.coerceIn(MinTextAreaRows, MaxTextAreaRows)?.toInt() ?: DefaultTextAreaRows

private const val MinTextAreaRows = 2.0
private const val MaxTextAreaRows = 100.0
private const val DefaultTextAreaRows = 3

/** `Input(name, placeholder?, type?, rules?, value?)` and `TextArea(name, placeholder?, rows?, rules?, value?)`. */
@Composable
internal fun TextInputComponent(ctx: ComponentRenderProps, multiline: Boolean) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val name = props.text("name").orEmpty()
    val componentType = if (multiline) "TextArea" else "Input"
    val formName = LocalOpenUIFormName.current
    val validation = LocalFormValidation.current
    val field = ctx.stateField(name, props["value"], componentType)
    val rules = remember(props["rules"]) { FormValidation.rules(props["rules"]) }
    val placeholder = props.text("placeholder").orEmpty()
    val type = props.string("type") ?: "text"
    var focused by remember { mutableStateOf(false) }
    var wasFocused by remember { mutableStateOf(false) }
    val textStyle = theme.typography.body.copy(color = theme.colors.textPrimary)

    BasicTextField(
        value = field.text(),
        onValueChange = { newValue ->
            field.setValue(OpenUIValue.Str(newValue))
            if (rules.isNotEmpty()) validation?.clearError(name)
        },
        enabled = !ctx.isStreaming,
        textStyle = textStyle,
        singleLine = !multiline,
        minLines = if (multiline) textAreaRows(props.number("rows")) else 1,
        cursorBrush = SolidColor(theme.colors.accent),
        visualTransformation = if (!multiline && type == "password") {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = if (multiline) {
            KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        } else {
            KeyboardOptions(
                keyboardType = when (type) {
                    "email" -> KeyboardType.Email
                    "number" -> KeyboardType.Decimal
                    "url" -> KeyboardType.Uri
                    "password" -> KeyboardType.Password
                    else -> KeyboardType.Text
                },
                capitalization = if (type == "email" || type == "url" || type == "password") {
                    KeyboardCapitalization.None
                } else {
                    KeyboardCapitalization.Sentences
                },
                autoCorrectEnabled = type != "email" && type != "url" && type != "password",
                imeAction = ImeAction.Done,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { state ->
                focused = state.isFocused
                if (state.isFocused) {
                    wasFocused = true
                    validation?.clearError(name)
                } else if (wasFocused && rules.isNotEmpty()) {
                    validation?.validateField(name, ctx.runtime.stateField(name, props["value"], formName).value, rules)
                }
            },
        decorationBox = { inner ->
            Box(Modifier.formFieldChrome(validation?.error(name) != null, focused)) {
                if (field.text().isEmpty()) Text(placeholder, style = textStyle, color = theme.colors.textTertiary)
                inner()
            }
        },
    )
    RegisterValidation(validation, name, rules, ctx.isStreaming) {
        ctx.runtime.stateField(name, props["value"], formName).value
    }
}

// Select ---------------------------------------------------------------------------------------

/** `Select(name, items, placeholder?, rules?, value?, size?)` with `SelectItem(value, label)`. */
@Composable
internal fun SelectComponent(ctx: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val name = props.text("name").orEmpty()
    val formName = LocalOpenUIFormName.current
    val validation = LocalFormValidation.current
    val field = ctx.stateField(name, props["value"], "Select")
    val rules = remember(props["rules"]) { FormValidation.rules(props["rules"]) }
    val items = props.elements("items").mapNotNull { item ->
        val value = item.props["value"]
        if (JS.isNullish(value)) return@mapNotNull null
        val text = JS.toString(value)
        if (text.isEmpty()) null else text to (item.props["label"]?.let(JS::toString) ?: text)
    }
    val selected = field.value.takeUnless(JS::isNullish)?.let(JS::toString)
    val selectedLabel = items.firstOrNull { it.first == selected }?.second
    var expanded by remember { mutableStateOf(false) }
    val style = if (props.string("size") == "small") theme.typography.bodySmall else theme.typography.body

    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(theme.radius.small))
                .clickable(enabled = !ctx.isStreaming, role = Role.DropdownList) { expanded = true }
                .formFieldChrome(validation?.error(name) != null, expanded),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                selectedLabel ?: props.text("placeholder")
                    ?: stringResource(R.string.stream_ai_openui_select_placeholder),
                style = style,
                color = if (selectedLabel == null) theme.colors.textTertiary else theme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(theme.spacing.s))
            Icon(Icons.Rounded.UnfoldMore, null, tint = theme.colors.textSecondary, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    trailingIcon = if (value == selected) ({ Icon(Icons.Rounded.Check, null) }) else null,
                    onClick = {
                        expanded = false
                        field.setValue(OpenUIValue.Str(value))
                        if (rules.isNotEmpty()) validation?.validateField(name, OpenUIValue.Str(value), rules)
                    },
                )
            }
        }
    }
    RegisterValidation(validation, name, rules, ctx.isStreaming) {
        ctx.runtime.stateField(name, props["value"], formName).value
    }
}

// Date picker ------------------------------------------------------------------------------------

/**
 * `yyyy-MM-dd` date strings used by date fields and date cells. A date is the UTC midnight of its day
 * (what the Material date picker selects), so the stored string never moves with the time zone.
 */
internal object FormDateFormatting {
    private fun formatter(zone: TimeZone = UTC) = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = zone
        isLenient = false
    }

    fun string(millis: Long): String = formatter().format(java.util.Date(millis))

    // A date-time like `2026-11-04T10:00:00Z` reads as its date: the fields edit days only.
    fun millis(string: String?): Long? {
        if (string.isNullOrEmpty()) return null
        return runCatching { formatter().parse(string.take(10))?.time }.getOrNull()
    }

    /** Today in the user's calendar ([zone]), as the UTC midnight of that day. */
    fun today(now: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): Long =
        millis(formatter(zone).format(java.util.Date(now)))!!

    /** The day as people read it in [locale], such as "Nov 4, 2026". */
    fun label(millis: Long, locale: Locale): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale).apply { timeZone = UTC }.format(java.util.Date(millis))

    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")
}

/** `DatePicker(name, mode?, rules?, value?)`: stores `yyyy-MM-dd`, or `{from, to}` for ranges. */
@Composable
internal fun DatePickerComponent(ctx: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val name = props.text("name").orEmpty()
    val formName = LocalOpenUIFormName.current
    val validation = LocalFormValidation.current
    val field = ctx.stateField(name, props["value"], "DatePicker")
    val rules = remember(props["rules"]) { FormValidation.rules(props["rules"]) }
    val isRange = props.string("mode") == "range"
    val shape = RoundedCornerShape(theme.radius.small)

    fun update(value: OpenUIValue) {
        field.setValue(value)
        if (rules.isNotEmpty()) validation?.validateField(name, value, rules)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(theme.colors.sunkenSurface, shape)
            .border(1.5.dp, if (validation?.error(name) != null) theme.colors.danger else Color.Transparent, shape)
            .padding(horizontal = theme.spacing.m, vertical = 6.dp)
            .alpha(if (ctx.isStreaming) 0.6f else 1f),
        verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
    ) {
        if (isRange) {
            val range = field.value.objectValue ?: OpenUIObject.Empty
            val from = FormDateFormatting.millis(range["from"].stringValue)
            val to = FormDateFormatting.millis(range["to"].stringValue)
            val enabled = !ctx.isStreaming
            FormDateRow(stringResource(R.string.stream_ai_openui_date_picker_from), from, null, enabled) { date ->
                var next = range.with("from", OpenUIValue.Str(FormDateFormatting.string(date)))
                if (to != null && to < date) next = next.with("to", OpenUIValue.Str(FormDateFormatting.string(date)))
                update(OpenUIValue.Obj(next))
            }
            FormDateRow(stringResource(R.string.stream_ai_openui_date_picker_to), to, from, enabled) { date ->
                var next = range.with("to", OpenUIValue.Str(FormDateFormatting.string(date)))
                if (range["from"] == null) next = next.with("from", OpenUIValue.Str(FormDateFormatting.string(date)))
                update(OpenUIValue.Obj(next))
            }
        } else {
            FormDateRow(
                stringResource(R.string.stream_ai_openui_date_picker_date),
                FormDateFormatting.millis(field.value.stringValue),
                minimum = null,
                enabled = !ctx.isStreaming,
            ) { date ->
                update(OpenUIValue.Str(FormDateFormatting.string(date)))
            }
        }
    }
    RegisterValidation(validation, name, rules, ctx.isStreaming) {
        ctx.runtime.stateField(name, props["value"], formName).value
    }
}

@Composable
private fun FormDateRow(title: String, date: Long?, minimum: Long?, enabled: Boolean, onChange: (Long) -> Unit) {
    val theme = OpenUITheme.current
    var open by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().heightIn(min = 36.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = theme.typography.bodySmall,
            color = theme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = { open = true }, enabled = enabled) {
            Text(
                date?.let { FormDateFormatting.label(it, currentLocale()) }
                    ?: stringResource(R.string.stream_ai_openui_date_picker_choose),
                style = theme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (date == null) theme.colors.accent else theme.colors.textPrimary,
            )
        }
    }
    if (open) {
        FormDatePickerDialog(date, minimum, onDismiss = { open = false }) { picked ->
            open = false
            onChange(picked)
        }
    }
}

// Slider ---------------------------------------------------------------------------------------

/**
 * `Slider(name, variant, min, max, step?, defaultValue?, label?, rules?, value?)`: stores `[number]`,
 * or `[lower, upper]` when its value or default has two numbers. The model writes the bounds, so
 * ones that aren't finite, or that leave no range, fall back to `0` to `100`.
 */
@Composable
internal fun SliderComponent(ctx: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val name = props.text("name").orEmpty()
    val formName = LocalOpenUIFormName.current
    val validation = LocalFormValidation.current
    val field = ctx.stateField(name, props["value"], "Slider")
    val rules = remember(props["rules"]) { FormValidation.rules(props["rules"]) }
    val bounds = SliderValues.bounds(props.number("min"), props.number("max"))
    val step = SliderValues.step(props.number("step"), props.string("variant"), bounds)
    val stored = SliderValues.values(field.value.takeUnless(JS::isNullish) ?: props["defaultValue"], bounds)
    var draft by remember { mutableStateOf<List<Double>?>(null) }
    val values = draft?.let { SliderValues.clamped(it, bounds) } ?: stored
    val label = props.text("label") ?: name
    val commit = { next: List<Double> ->
        val value = SliderValues.stored(next)
        field.setValue(value)
        if (rules.isNotEmpty()) validation?.validateField(name, value.arrayValue?.firstOrNull(), rules)
        draft = null
    }

    Column(
        Modifier.fillMaxWidth().alpha(if (ctx.isStreaming) DisabledAlpha else 1f),
        verticalArrangement = Arrangement.spacedBy(theme.spacing.xs),
    ) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics { }) {
            Text(
                label,
                style = theme.typography.bodySmall,
                color = theme.colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                values.joinToString(" – ", transform = NumberFormatting::decimal),
                style = theme.typography.bodySmall.merge(TabularNumbers).copy(fontWeight = FontWeight.SemiBold),
                color = theme.colors.textPrimary,
            )
        }
        SliderTrack(values, bounds, step, label, enabled = !ctx.isStreaming, onChange = { draft = it }) {
            draft?.let(commit)
        }
        Row(Modifier.fillMaxWidth().clearAndSetSemantics { }) {
            val small = theme.typography.caption.merge(TabularNumbers).copy(fontSize = 11.sp)
            Text(
                NumberFormatting.decimal(bounds.start),
                style = small,
                color = theme.colors.textTertiary,
                modifier = Modifier.weight(1f),
            )
            Text(NumberFormatting.decimal(bounds.endInclusive), style = small, color = theme.colors.textTertiary)
        }
    }
    RegisterValidation(validation, name, rules, ctx.isStreaming) {
        ctx.runtime.stateField(name, props["value"], formName).value
    }
}

// One slider, or a range slider for two values, on a plain track: no dot per step.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderTrack(
    values: List<Double>,
    bounds: ClosedFloatingPointRange<Double>,
    step: Double?,
    label: String,
    enabled: Boolean,
    onChange: (List<Double>) -> Unit,
    onChangeFinished: () -> Unit,
) {
    val theme = OpenUITheme.current
    val range = bounds.start.toFloat()..bounds.endInclusive.toFloat()
    val steps = SliderValues.tickCount(bounds, step)
    val snap = { raw: Float -> SliderValues.snapped(raw.toDouble(), bounds, step) }
    val colors = SliderDefaults.colors(
        thumbColor = theme.colors.accent,
        activeTrackColor = theme.colors.accent,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
        disabledActiveTickColor = Color.Transparent,
        disabledInactiveTickColor = Color.Transparent,
    )
    val valueText = values.map(NumberFormatting::decimal)
    if (values.size == 2) {
        val lower = stringResource(R.string.stream_ai_openui_slider_lower, label)
        val upper = stringResource(R.string.stream_ai_openui_slider_upper, label)
        val startSource = remember { MutableInteractionSource() }
        val endSource = remember { MutableInteractionSource() }
        RangeSlider(
            value = values[0].toFloat()..values[1].toFloat(),
            onValueChange = { next -> onChange(listOf(snap(next.start), snap(next.endInclusive))) },
            onValueChangeFinished = onChangeFinished,
            valueRange = range,
            steps = steps,
            enabled = enabled,
            colors = colors,
            startInteractionSource = startSource,
            endInteractionSource = endSource,
            startThumb = {
                SliderDefaults.Thumb(
                    startSource,
                    colors = colors,
                    enabled = enabled,
                    modifier = Modifier.semantics {
                        contentDescription = lower
                        stateDescription = valueText[0]
                    },
                )
            },
            endThumb = {
                SliderDefaults.Thumb(
                    endSource,
                    colors = colors,
                    enabled = enabled,
                    modifier = Modifier.semantics {
                        contentDescription = upper
                        stateDescription = valueText[1]
                    },
                )
            },
        )
    } else {
        Slider(
            value = values.first().toFloat(),
            onValueChange = { onChange(listOf(snap(it))) },
            onValueChangeFinished = onChangeFinished,
            valueRange = range,
            steps = steps,
            enabled = enabled,
            colors = colors,
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = valueText.first()
            },
        )
    }
}

// Toggle groups ---------------------------------------------------------------------------------

/** A title with an optional secondary line (inline Markdown in both). */
@Composable
private fun FormTitleSubtitle(title: String, subtitle: String?, modifier: Modifier = Modifier, bold: Boolean = false) {
    val theme = OpenUITheme.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            inlineMarkdown(title, theme.colors.accent),
            style = theme.typography.bodySmall.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Medium),
            color = theme.colors.textPrimary,
        )
        if (!subtitle.isNullOrEmpty()) {
            Text(
                inlineMarkdown(subtitle, theme.colors.accent),
                style = theme.typography.caption,
                color = theme.colors.textSecondary,
            )
        }
    }
}

/**
 * `CheckBoxGroup(name, items, rules?, value?)` and `SwitchGroup(name, items, variant?, value?)`: store
 * `{itemName: bool}`.
 */
@Composable
internal fun ToggleGroupComponent(ctx: ComponentRenderProps, isSwitch: Boolean) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val name = props.text("name").orEmpty()
    val componentType = if (isSwitch) "SwitchGroup" else "CheckBoxGroup"
    val formName = LocalOpenUIFormName.current
    val validation = LocalFormValidation.current
    val field = ctx.stateField(name, props["value"], componentType)
    val rules = if (isSwitch) emptyList() else remember(props["rules"]) { FormValidation.rules(props["rules"]) }
    val items = props.elements("items")
    if (items.isEmpty()) return
    val stored = field.value.objectValue
    val aggregate = OpenUIObject.build {
        for (item in items) {
            val itemName = item.props["name"]?.let(JS::toString) ?: continue
            this[itemName] =
                OpenUIValue.of(stored?.get(itemName).boolValue ?: item.props["defaultChecked"].boolValue ?: false)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .openUISurface(if (isSwitch) props.string("variant") ?: "clear" else "clear", padding = theme.spacing.m),
    ) {
        items.forEachIndexed { index, item ->
            val itemProps = ComponentProps(item.props)
            val itemName = itemProps.text("name") ?: "$index"
            val isOn = aggregate[itemName].boolValue ?: false
            val toggle = { newValue: Boolean ->
                val next = OpenUIValue.Obj(aggregate.with(itemName, OpenUIValue.of(newValue)))
                field.setValue(next)
                if (rules.isNotEmpty()) validation?.validateField(name, next, rules)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = isOn,
                        enabled = !ctx.isStreaming,
                        role = if (isSwitch) Role.Switch else Role.Checkbox,
                        onValueChange = toggle,
                    )
                    .padding(vertical = theme.spacing.s),
                verticalAlignment = if (isSwitch) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.m),
            ) {
                if (isSwitch) {
                    FormTitleSubtitle(
                        itemProps.text("label") ?: itemName,
                        itemProps.text("description"),
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = isOn,
                        onCheckedChange = null,
                        enabled = !ctx.isStreaming,
                        colors = SwitchDefaults.colors(checkedTrackColor = theme.colors.accent),
                    )
                } else {
                    Checkbox(
                        checked = isOn,
                        onCheckedChange = null,
                        enabled = !ctx.isStreaming,
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(checkedColor = theme.colors.accent),
                    )
                    FormTitleSubtitle(
                        itemProps.text("label") ?: itemName,
                        itemProps.text("description"),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (isSwitch && index < items.lastIndex) HorizontalDivider(color = theme.colors.border)
        }
    }
    RegisterValidation(validation, name, rules, ctx.isStreaming) {
        ctx.runtime.stateField(name, props["value"], formName).value ?: OpenUIValue.Obj(aggregate)
    }
}

/** `RadioGroup(name, items, defaultValue?, rules?, value?)` with `RadioItem(label, description, value)`. */
@Composable
internal fun RadioGroupComponent(ctx: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val name = props.text("name").orEmpty()
    val formName = LocalOpenUIFormName.current
    val validation = LocalFormValidation.current
    val field = ctx.stateField(name, props["value"], "RadioGroup")
    val rules = remember(props["rules"]) { FormValidation.rules(props["rules"]) }
    val items = props.elements("items")
    if (items.isEmpty()) return
    val current = (field.value.takeUnless(JS::isNullish) ?: props["defaultValue"])?.let(JS::toString)

    Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) {
        items.forEach { item ->
            val itemProps = ComponentProps(item.props)
            val value = itemProps.text("value").orEmpty()
            val selected = value == current
            val select = {
                field.setValue(OpenUIValue.Str(value))
                if (rules.isNotEmpty()) validation?.validateField(name, OpenUIValue.Str(value), rules)
            }
            Row(
                Modifier.fillMaxWidth().selectable(
                    selected = selected,
                    enabled = !ctx.isStreaming,
                    role = Role.RadioButton,
                    onClick = select,
                ),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.m),
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    enabled = !ctx.isStreaming,
                    modifier = Modifier.size(24.dp),
                    colors = RadioButtonDefaults.colors(selectedColor = theme.colors.accent),
                )
                FormTitleSubtitle(
                    itemProps.text("label") ?: value,
                    itemProps.text("description"),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    RegisterValidation(validation, name, rules, ctx.isStreaming) {
        ctx.runtime.stateField(name, props["value"], formName).value
    }
}

// Chips & option cards ----------------------------------------------------------------------------

/** `Chips(...)` and `OptionCards(...)`: single or multiple selection stored under `name`. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectionFieldComponent(ctx: ComponentRenderProps, isCards: Boolean) {
    val theme = OpenUITheme.current
    val props = ctx.props
    val name = props.text("name").orEmpty()
    val componentType = if (isCards) "OptionCards" else "Chips"
    val single = (props.string("type") ?: if (isCards) "single" else "multiple") == "single"
    val formName = LocalOpenUIFormName.current
    val validation = LocalFormValidation.current
    val rules = remember(props["rules"]) { FormValidation.rules(props["rules"]) }
    val items = props.elements("items").filter { !JS.isNullish(it.props["value"]) }
    LocalOpenUIStoreSnapshot.current // Recompose when the selection changes.
    val stored = ctx.runtime.fieldValue(formName, name)
    val selection = SelectionField.normalize(single, stored, props["defaultValue"])
    val runtime = ctx.runtime

    fun toggle(item: ElementNode) {
        if (ctx.isStreaming || item.props["disabled"] == OpenUIValue.True) return
        val next = SelectionField.toggle(single, selection, JS.toString(item.props["value"]))
        val value: OpenUIValue = if (single) {
            next.firstOrNull()?.let(OpenUIValue::Str) ?: OpenUIValue.Null
        } else {
            OpenUIValue.Arr(next.map(OpenUIValue::Str))
        }
        runtime.setFieldValue(formName, componentType, name, value)
        if (rules.isNotEmpty()) {
            val validationValue = if (single) {
                next.firstOrNull()?.let(OpenUIValue::Str)
            } else {
                value.takeIf { next.isNotEmpty() }
            }
            validation?.validateField(name, validationValue, rules)
        }
    }

    if (isCards) {
        ResponsiveGrid(
            modifier = if (single) Modifier.selectableGroup() else Modifier,
            spacing = theme.spacing.s,
            minItemWidth = 140.dp,
            maxColumns = 3,
        ) {
            items.forEach { item ->
                FormOptionCard(item, selection, single, enabled = !ctx.isStreaming) { toggle(item) }
            }
        }
    } else {
        FlowRow(
            modifier = if (single) Modifier.selectableGroup() else Modifier,
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.s),
            verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
        ) {
            items.forEach { item -> FormChip(item, selection, single, enabled = !ctx.isStreaming) { toggle(item) } }
        }
    }

    // Persist the default selection once streaming has finished (useHydrateDefaultFieldValue).
    LaunchedEffect(ctx.isStreaming, formName, name) {
        if (ctx.isStreaming || runtime.fieldValue(formName, name) != null) return@LaunchedEffect
        SelectionField.storedDefault(single, props["defaultValue"])?.let {
            runtime.setFieldValue(formName, componentType, name, it)
        }
    }
    RegisterValidation(validation, name, rules, ctx.isStreaming) {
        val value = runtime.fieldValue(formName, name) ?: SelectionField.storedDefault(single, props["defaultValue"])
        when {
            single -> value.takeIf { it is OpenUIValue.Str && it.value.isNotEmpty() }
            value is OpenUIValue.Arr && value.items.isNotEmpty() -> value
            else -> null
        }
    }
}

@Composable
private fun FormChip(
    item: ElementNode,
    selection: List<String>,
    single: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val theme = OpenUITheme.current
    val props = ComponentProps(item.props)
    val value = JS.toString(item.props["value"])
    val selected = value in selection
    val disabled = props.bool("disabled") ?: false
    val shape = RoundedCornerShape(theme.radius.pill)
    val content = if (selected) theme.colors.accent else theme.colors.textPrimary
    Row(
        Modifier
            .alpha(if (disabled) 0.5f else 1f)
            .clip(shape)
            .background(if (selected) theme.colors.accent.copy(alpha = 0.14f) else theme.colors.sunkenSurface, shape)
            .border(1.dp, if (selected) theme.colors.accent else Color.Transparent, shape)
            .selectionItem(selected, single, enabled = enabled && !disabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        props.element("icon")?.let { OpenUIIcon(it, Modifier.size(14.dp), tint = content, decorative = true) }
        Text(
            props.text("label") ?: value,
            style = theme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = content,
        )
        if (selected) Icon(Icons.Rounded.Check, null, tint = content, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun FormOptionCard(
    item: ElementNode,
    selection: List<String>,
    single: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val theme = OpenUITheme.current
    val props = ComponentProps(item.props)
    val value = JS.toString(item.props["value"])
    val selected = value in selection
    val disabled = props.bool("disabled") ?: false
    val top = props.element("topContent")
    val shape = RoundedCornerShape(theme.radius.medium)
    Box(
        Modifier
            .fillMaxWidth()
            .alpha(if (disabled) 0.5f else 1f)
            .clip(shape)
            .background(if (selected) theme.colors.accent.copy(alpha = 0.08f) else theme.colors.sunkenSurface, shape)
            .border(1.5.dp, if (selected) theme.colors.accent else Color.Transparent, shape)
            .selectionItem(selected, single, enabled = enabled && !disabled, onClick = onClick),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(theme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
        ) {
            if (top != null) {
                val src = top.props["src"].stringValue
                if (src != null) {
                    RemoteImage(
                        url = src,
                        alt = top.props["alt"].stringValue,
                        modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(theme.radius.small)),
                    )
                } else {
                    FormIconBadge(top, soft = selected)
                }
            }
            FormTitleSubtitle(props.text("title") ?: value, props.text("subtitle"), bold = true)
        }
        if (selected) {
            Icon(
                Icons.Rounded.CheckCircle,
                null,
                tint = theme.colors.accent,
                modifier = Modifier.align(Alignment.TopEnd).padding(theme.spacing.s).size(20.dp),
            )
        }
    }
}

/**
 * One option of a selection field: a radio button when one option can be chosen, a checkbox when
 * several can, so accessibility services read it as one control with its selected state.
 */
private fun Modifier.selectionItem(selected: Boolean, single: Boolean, enabled: Boolean, onClick: () -> Unit) =
    if (single) {
        selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
    } else {
        toggleable(value = selected, enabled = enabled, role = Role.Checkbox) { onClick() }
    }

/** A small rounded icon badge (`IconBadge` with the `sm` size, `soft` or `neutral` variant). */
@Composable
private fun FormIconBadge(icon: ElementNode, soft: Boolean) {
    val theme = OpenUITheme.current
    val dimension = 30.dp
    Box(
        Modifier
            .size(dimension)
            .background(
                if (soft) theme.colors.accent.copy(alpha = 0.12f) else theme.colors.sunkenSurface,
                RoundedCornerShape(dimension * 0.28f),
            ),
        contentAlignment = Alignment.Center,
    ) {
        OpenUIIcon(
            icon,
            Modifier.size(dimension * 0.5f),
            tint = if (soft) theme.colors.accent else theme.colors.textPrimary,
            decorative = true,
        )
    }
}

private val TabularNumbers = TextStyle(fontFeatureSettings = "tnum")
private const val DisabledAlpha = 0.6f
