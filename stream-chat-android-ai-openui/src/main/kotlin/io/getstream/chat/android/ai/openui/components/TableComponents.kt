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

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.rendering.ComponentProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.utils.NumberFormatting
import java.util.Locale
import kotlin.math.ceil

internal val tableRenderers: Map<String, ComponentRenderer> = mapOf(
    "Table" to { TableComponent(it) },
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

// The columns and rows a loading table shows, and how long one pulse of it takes.
private const val SkeletonMinColumns = 3
private const val SkeletonRows = 5
private const val SkeletonPulseMillis = 800
