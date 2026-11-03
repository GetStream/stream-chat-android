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

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIJson
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.numberValue
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.plainJson
import io.getstream.chat.android.ai.openui.lang.stringValue
import io.getstream.chat.android.ai.openui.rendering.ComponentProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.utils.NumberFormatting
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil

internal val tableRenderers: Map<String, ComponentRenderer> = mapOf(
    "Table" to { TableComponent(it) },
    "EditableTable" to { EditableTableComponent(it) },
)

// Table ---------------------------------------------------------------------------------------

private const val TablePageSize = 10

/** The widest a cell grows; longer text wraps and wider elements scroll. */
private val TableCellMaxWidth = 260.dp

private class TableColumn(val label: String, val data: List<OpenUIValue>, val isNumeric: Boolean)

/** `Table(columns)` with `Col(label, data, type?)`: column-oriented, 10 rows per page. */
// Sorting, paging and the loading state all feed the one grid it draws.
@Suppress("LongMethod")
@Composable
private fun TableComponent(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    var page by rememberSaveable { mutableIntStateOf(0) }
    val columns = context.props.elements("columns").map { column ->
        val props = ComponentProps(column.props)
        val data = props.array("data")
        val declaredNumeric = props.string("type") == "number"
        val looksNumeric = data.isNotEmpty() && data.all { it is OpenUIValue.Num || it is OpenUIValue.Null }
        TableColumn(props.text("label").orEmpty(), data, declaredNumeric || looksNumeric)
    }
    val rowCount = columns.maxOfOrNull { it.data.size } ?: 0

    if (context.runtime.isQueryLoading && rowCount == 0) {
        TableSkeleton(maxOf(columns.size, SkeletonMinColumns))
        return
    }
    if (columns.isEmpty()) return

    val pages = maxOf(1, ceil(rowCount.toDouble() / TablePageSize).toInt())
    val current = minOf(page, pages - 1)
    val rows = (current * TablePageSize until minOf(rowCount, (current + 1) * TablePageSize)).toList()

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
    ) {
        // Full width when the table fits; horizontally scrollable otherwise.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val available = maxWidth
            Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                val shape = RoundedCornerShape(theme.radius.small)
                TableGrid(
                    columns = columns.size,
                    rows = rows.size + 1,
                    minWidth = available,
                    trailing = columns.map { it.isNumeric },
                    headerColor = theme.colors.subtleFill,
                    dividerColor = theme.colors.border,
                    modifier = Modifier.clip(shape).border(0.5.dp, theme.colors.border, shape),
                ) {
                    columns.forEach { column ->
                        Text(
                            column.label.uppercase(Locale.getDefault()),
                            style = theme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                            color = theme.colors.textSecondary,
                            modifier = Modifier.padding(horizontal = theme.spacing.m, vertical = theme.spacing.s),
                        )
                    }
                    rows.forEach { row ->
                        columns.forEach { column ->
                            Box(Modifier.padding(horizontal = theme.spacing.m, vertical = theme.spacing.s + 2.dp)) {
                                TableCell(context, column.data.getOrNull(row), column.isNumeric)
                            }
                        }
                    }
                }
            }
        }
        if (pages > 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.s),
            ) {
                FilledTonalIconButton(onClick = {
                    page = maxOf(0, current - 1)
                }, enabled = current > 0, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.stream_ai_openui_pagination_previous_page),
                        modifier = Modifier.size(18.dp),
                    )
                }
                val position = NumberFormatting.integer(current + 1)
                val count = NumberFormatting.integer(pages)
                val spoken = stringResource(
                    R.string.stream_ai_openui_pagination_position_accessibility,
                    position,
                    count,
                )
                Text(
                    stringResource(R.string.stream_ai_openui_pagination_position, position, count),
                    style = theme.typography.caption.copy(fontFeatureSettings = "tnum"),
                    color = theme.colors.textSecondary,
                    modifier = Modifier.semantics { contentDescription = spoken },
                )
                FilledTonalIconButton(onClick = {
                    page = minOf(pages - 1, current + 1)
                }, enabled = current < pages - 1, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.stream_ai_openui_pagination_next_page),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TableCell(context: ComponentRenderProps, value: OpenUIValue?, numeric: Boolean) {
    val theme = OpenUITheme.current
    when {
        // The grid scrolls horizontally, so cells get no width bound from it. Elements need one:
        // anything that scrolls horizontally (code, tabs, carousels, tables) can't be measured
        // with an infinite max width.
        value is OpenUIValue.Element || value is OpenUIValue.Arr -> Row(
            modifier = Modifier.widthIn(max = TableCellMaxWidth),
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.xs),
        ) {
            context.RenderNode(value)
        }
        value != null && value !is OpenUIValue.Null -> Text(
            JS.toString(value),
            style = if (numeric) {
                theme.typography.bodySmall.copy(fontFeatureSettings = "tnum")
            } else {
                theme.typography.bodySmall
            },
            color = theme.colors.textPrimary,
            modifier = Modifier.widthIn(max = TableCellMaxWidth),
        )
        else -> Text("—", style = theme.typography.bodySmall, color = theme.colors.textTertiary)
    }
}

/**
 * A grid of `rows × columns` cells given row by row (the first row is the header). Columns take
 * their widest cell; spare width up to [minWidth] is shared out. Cells are measured once with
 * loose constraints (no intrinsics), then aligned inside their slot.
 */
// The grid measures every cell and draws its header and dividers in one layout pass.
@Suppress("LongMethod", "LongParameterList")
@Composable
private fun TableGrid(
    columns: Int,
    rows: Int,
    minWidth: Dp,
    trailing: List<Boolean>,
    headerColor: androidx.compose.ui.graphics.Color,
    dividerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val rowBottoms = remember { FloatArrayHolder() }
    Layout(
        content,
        modifier.drawBehind {
            val bottoms = rowBottoms.values
            if (bottoms.isNotEmpty()) {
                drawRect(headerColor, Offset.Zero, Size(size.width, bottoms[0]))
                for (index in 0 until bottoms.size - 1) {
                    drawLine(
                        dividerColor,
                        Offset(0f, bottoms[index]),
                        Offset(size.width, bottoms[index]),
                        strokeWidth = 0.5.dp.toPx(),
                    )
                }
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(
                Constraints(maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity),
            )
        }
        val widths = IntArray(columns)
        val heights = IntArray(rows)
        placeables.forEachIndexed { index, placeable ->
            val row = index / columns
            val column = index % columns
            if (row < rows) {
                widths[column] = maxOf(widths[column], placeable.width)
                heights[row] = maxOf(heights[row], placeable.height)
            }
        }
        val natural = widths.sum()
        val target = maxOf(natural, minWidth.roundToPx(), constraints.minWidth)
        if (target > natural && columns > 0) {
            val extra = (target - natural) / columns
            for (column in 0 until columns) widths[column] += extra
            widths[columns - 1] += (target - natural) - extra * columns
        }
        val totalHeight = heights.sum()
        var bottom = 0f
        rowBottoms.values = FloatArray(rows) { row ->
            bottom += heights[row]
            bottom
        }
        layout(target, totalHeight) {
            var y = 0
            for (row in 0 until rows) {
                var x = 0
                for (column in 0 until columns) {
                    val placeable = placeables.getOrNull(row * columns + column) ?: continue
                    val dx = if (trailing.getOrElse(column) { false }) widths[column] - placeable.width else 0
                    placeable.placeRelative(x + dx, y + (heights[row] - placeable.height) / 2)
                    x += widths[column]
                }
                y += heights[row]
            }
        }
    }
}

private class FloatArrayHolder {
    var values: FloatArray = FloatArray(0)
}

/** Placeholder rows shown while a query loads. */
@Composable
private fun TableSkeleton(columns: Int) {
    val theme = OpenUITheme.current
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(SkeletonPulseMillis), RepeatMode.Reverse),
        label = "pulse",
    )
    val loading = stringResource(R.string.stream_ai_openui_table_loading)
    Column(
        Modifier.fillMaxWidth().alpha(pulse).semantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
    ) {
        repeat(SkeletonRows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(theme.spacing.m)) {
                repeat(columns) {
                    Box(
                        Modifier
                            .weight(1f).height(14.dp)
                            .background(theme.colors.sunkenSurface, RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
    }
}

// EditableTable -------------------------------------------------------------------------------

private class EditableColumn(
    val type: String,
    val key: String,
    val header: String,
    val width: Double?,
    val options: List<Pair<String, String>>,
)

/** `EditableTable(name?, columns?, data?)`: inline editable cells, saved as a form field. */
@Composable
private fun EditableTableComponent(context: ComponentRenderProps) {
    val theme = OpenUITheme.current
    val tableName = context.props.text("name").orEmpty()
    val columns = context.props.array("columns").mapNotNull { it.objectValue }.map { column ->
        EditableColumn(
            type = column["type"].stringValue ?: "text",
            key = column["key"].stringValue ?: "default",
            header = column["header"].stringValue.orEmpty(),
            width = column["width"].numberValue,
            options = column["options"].arrayValue.orEmpty().mapNotNull { option ->
                val value = option["value"].stringValue ?: return@mapNotNull null
                value to (option["label"].stringValue ?: value)
            },
        )
    }
    var rows by remember { mutableStateOf(emptyList<Map<String, OpenUIValue>>()) }
    var baseline by remember { mutableStateOf(emptyList<Map<String, OpenUIValue>>()) }
    var loadedSignature by remember { mutableStateOf("") }

    val dataSignature = OpenUIJson.stringify(context.props["data"]?.plainJson ?: OpenUIValue.Null)
    LaunchedEffect(dataSignature, columns.map { it.key }) {
        val stored = context.runtime.fieldValue(tableName, tableName)
        val source = if (JS.isNullish(stored)) context.props["data"] else stored
        val signature = OpenUIJson.stringify(source?.plainJson ?: OpenUIValue.Null)
        if (signature == loadedSignature) return@LaunchedEffect
        loadedSignature = signature
        val parsed = source.arrayValue.orEmpty().map { row ->
            val cells = row["values"].arrayValue.orEmpty()
            buildMap {
                put("id", row["id"] ?: OpenUIValue.Str(""))
                columns.forEachIndexed { index, column ->
                    put(column.key, cells.getOrNull(index) ?: OpenUIValue.Str(""))
                }
            }
        }
        rows = parsed
        if (baseline.isEmpty() || !context.isStreaming) baseline = parsed
    }

    val changed = rows.withIndex().sumOf { (index, row) ->
        val base = baseline.getOrNull(index) ?: return@sumOf 0
        columns.count { row[it.key] != base[it.key] }
    }
    val enabled = !context.isStreaming

    fun update(row: Int, key: String, value: OpenUIValue) {
        if (row !in rows.indices) return
        rows = rows.toMutableList().also { it[row] = it[row] + (key to value) }
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) {
        val shape = RoundedCornerShape(theme.radius.small)
        Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Column(Modifier.clip(shape).border(0.5.dp, theme.colors.border, shape)) {
                Row(Modifier.background(theme.colors.subtleFill)) {
                    columns.forEach { column ->
                        Text(
                            column.header,
                            style = theme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                            color = theme.colors.textSecondary,
                            modifier = Modifier
                                .padding(horizontal = theme.spacing.s, vertical = theme.spacing.s)
                                .width((column.width ?: 150.0).dp),
                        )
                    }
                }
                rows.indices.forEach { rowIndex ->
                    HorizontalDivider(color = theme.colors.border, thickness = 0.5.dp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        columns.forEach { column ->
                            Box(
                                Modifier
                                    .padding(horizontal = theme.spacing.s, vertical = theme.spacing.xs)
                                    .width((column.width ?: 150.0).dp),
                            ) {
                                val text = rows
                                    .getOrNull(rowIndex)
                                    ?.get(column.key)
                                    ?.let(JS::toStringOrEmpty)
                                    .orEmpty()
                                EditableCell(column, text, enabled) { newValue ->
                                    val value = if (column.type == "number") {
                                        val number = JS.stringToNumber(newValue)
                                        if (number.isNaN() || newValue.isEmpty()) {
                                            OpenUIValue.Str(newValue)
                                        } else {
                                            OpenUIValue.Num(number)
                                        }
                                    } else {
                                        OpenUIValue.Str(newValue)
                                    }
                                    update(rowIndex, column.key, value)
                                }
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(changed > 0) {
            val save = stringResource(R.string.stream_ai_openui_editable_table_save)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.s),
            ) {
                Text(
                    pluralStringResource(R.plurals.stream_ai_openui_editable_table_changes, changed, changed),
                    style = theme.typography.caption,
                    color = theme.colors.textSecondary,
                )
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = { rows = baseline },
                    enabled = enabled,
                ) {
                    val reset = stringResource(R.string.stream_ai_openui_editable_table_reset)
                    Text(reset, style = theme.typography.caption)
                }
                Button(
                    onClick = {
                        val data = rows.map { row ->
                            OpenUIValue.obj(
                                "id" to (row["id"] ?: OpenUIValue.Str("")),
                                "values" to OpenUIValue.Arr(columns.map { row[it.key] ?: OpenUIValue.Str("") }),
                            )
                        }
                        context.runtime.setFieldValue(tableName, "EditableTable", tableName, OpenUIValue.Arr(data))
                        context.triggerAction(save, formName = tableName)
                        baseline = rows
                    },
                    enabled = enabled,
                ) { Text(save, style = theme.typography.caption) }
            }
        }
    }
}

@Composable
private fun EditableCell(column: EditableColumn, text: String, enabled: Boolean, onChange: (String) -> Unit) {
    val theme = OpenUITheme.current
    when (column.type) {
        "select" -> {
            var expanded by remember { mutableStateOf(false) }
            Box {
                Row(
                    Modifier.fillMaxWidth().clickable(enabled = enabled) { expanded = true }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        column.options.firstOrNull { it.first == text }?.second ?: text,
                        style = theme.typography.bodySmall,
                        color = theme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Rounded.ArrowDropDown,
                        contentDescription = column.header,
                        tint = theme.colors.textSecondary,
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    column.options.forEach { (value, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            expanded = false
                            onChange(value)
                        })
                    }
                }
            }
        }
        "date-single" -> {
            val context = LocalContext.current
            Text(
                text.ifEmpty { column.header },
                style = theme.typography.bodySmall,
                color = if (text.isEmpty()) theme.colors.textTertiary else theme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        val calendar = Calendar.getInstance()
                        TableDateFormatting.date(text)?.let { calendar.time = it }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val picked = Calendar.getInstance().apply { set(year, month, day) }
                                onChange(TableDateFormatting.string(picked.time))
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    }.padding(vertical = 6.dp),
            )
        }
        else -> BasicTextField(
            value = text,
            onValueChange = onChange,
            enabled = enabled,
            singleLine = true,
            textStyle = theme.typography.bodySmall.copy(color = theme.colors.textPrimary),
            cursorBrush = SolidColor(theme.colors.accent),
            keyboardOptions = KeyboardOptions(
                keyboardType = when (column.type) {
                    "number" -> KeyboardType.Decimal
                    "url" -> KeyboardType.Uri
                    else -> KeyboardType.Text
                },
            ),
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) {
                        Text(column.header, style = theme.typography.bodySmall, color = theme.colors.textTertiary)
                    }
                    inner()
                }
            },
        )
    }
}

/** `yyyy-MM-dd` date strings used by date cells. */
internal object TableDateFormatting {
    private fun formatter() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun string(date: Date): String = formatter().format(date)

    fun date(string: String?): Date? {
        if (string.isNullOrEmpty()) return null
        try {
            return formatter().parse(string.take(10))
        } catch (_: ParseException) {
        }
        return try {
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                Locale.US,
            ).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(string)
        } catch (_: ParseException) {
            null
        }
    }
}

// The columns and rows a loading table shows, and how long one pulse of it takes.
private const val SkeletonMinColumns = 3
private const val SkeletonRows = 5
private const val SkeletonPulseMillis = 800
