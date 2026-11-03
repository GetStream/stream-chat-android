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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.ai.openui.R
import io.getstream.chat.android.ai.openui.lang.JS
import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.elementValue
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderProps
import io.getstream.chat.android.ai.openui.rendering.ComponentRenderer
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.utils.NumberFormatting
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// Charts are drawn with Compose Canvas; no chart library.

internal val chartRenderers: Map<String, ComponentRenderer> = mapOf(
    "BarChart" to { CartesianChart(it, CartesianKind.Bar) },
    "LineChart" to { CartesianChart(it, CartesianKind.Line) },
    "AreaChart" to { CartesianChart(it, CartesianKind.Area) },
    "HorizontalBarChart" to { CartesianChart(it, CartesianKind.HorizontalBar) },
    "RadarChart" to { RadarChart(it) },
    "PieChart" to { PieChart(it) },
    "RadialChart" to { RadialChart(it) },
    "SingleStackedBarChart" to { SingleStackedBarChart(it) },
    "ScatterChart" to { ScatterChart(it) },
)

// Data ------------------------------------------------------------------------------------------

/** One value of a named series at a category. */
internal data class ChartPoint(val category: String, val series: String, val value: Double, val order: Int)

internal data class ChartSlice(val label: String, val value: Double)

internal object ChartData {
    // Beyond this a chart can't place values apart on a screen.
    private const val LARGEST_VALUE = 1e15

    /** [value] when a chart can draw it: finite and of a size a screen can show, `NaN` otherwise. */
    fun drawable(value: Double): Double = if (value.isFinite() && abs(value) <= LARGEST_VALUE) value else Double.NaN

    /** Port of `buildChartData`: `labels` + `Series` elements, or tabular rows (`[[label, v1, v2], ...]`). */
    fun points(labels: List<OpenUIValue>, series: List<OpenUIValue>): Pair<List<ChartPoint>, List<String>> {
        val labelStrings = labels.map(JS::toStringOrEmpty)
        if (series.firstOrNull() is OpenUIValue.Arr) {
            val seriesNames = labelStrings.drop(1)
            val points = series.flatMapIndexed { rowIndex, row ->
                val cells = row.arrayValue.orEmpty()
                val category = cells.firstOrNull()?.let(JS::toStringOrEmpty).orEmpty()
                seriesNames.mapIndexed { seriesIndex, name ->
                    ChartPoint(category, name, drawable(JS.toNumber(cells.getOrNull(seriesIndex + 1))), rowIndex)
                }
            }
            return points to seriesNames
        }
        val names = mutableListOf<String>()
        val points = mutableListOf<ChartPoint>()
        for (element in series.mapNotNull { it.elementValue }) {
            val name = (element.props["category"] as? OpenUIValue.Str)?.value ?: continue
            val values = element.props["values"].arrayValue ?: continue
            if (name !in names) names += name
            labelStrings.forEachIndexed { index, label ->
                if (index < values.size) points += ChartPoint(label, name, drawable(JS.toNumber(values[index])), index)
            }
        }
        return points to names
    }

    /** `labels` + `values`, falling back to legacy `Slice` elements. A value a chart can't draw is `0`. */
    fun slices(labels: List<OpenUIValue>, values: List<OpenUIValue>): List<ChartSlice> {
        if (labels.isNotEmpty() && values.isNotEmpty()) {
            return labels.mapIndexed { index, label ->
                val value = (values.getOrNull(index) as? OpenUIValue.Num)?.value ?: 0.0
                ChartSlice(JS.toStringOrEmpty(label), drawable(value).orZero())
            }
        }
        return labels.mapNotNull { it.elementValue }.map { slice ->
            val label = slice.props["category"]?.let(JS::toString).orEmpty()
            ChartSlice(label, drawable(JS.toNumber(slice.props["value"])).orZero())
        }
    }

    fun summary(points: List<ChartPoint>, series: List<String>): String = series.joinToString(". ") { name ->
        "$name: " +
            points
                .filter { it.series == name }
                .joinToString(", ") { "${it.category} ${NumberFormatting.decimal(it.value)}" }
    }
}

/** A chart's `height` when the model gives a finite positive one, kept to at most 1000. */
internal fun chartHeight(height: Double?): Double? =
    height?.takeIf { it.isFinite() && it > 0 }?.coerceAtMost(MaxChartHeight)

private const val MaxChartHeight = 1000.0

/** Round axis ticks covering `min..max` (the "nice numbers" algorithm). */
internal fun niceTicks(minValue: Double, maxValue: Double, count: Int = 4): List<Double> {
    var low = minValue
    var high = maxValue
    if (!low.isFinite() || !high.isFinite() || !(high - low).isFinite()) return listOf(0.0, 1.0)
    if (high == low) {
        if (high == 0.0) {
            high = 1.0
        } else if (high > 0) {
            low = 0.0
        } else {
            high = 0.0
        }
    }
    fun nice(range: Double, round: Boolean): Double {
        val exponent = floor(log10(range))
        val fraction = range / 10.0.pow(exponent)
        val niceFraction = if (round) {
            when {
                fraction < 1.5 -> 1.0
                fraction < 3 -> 2.0
                fraction < 7 -> 5.0
                else -> 10.0
            }
        } else {
            when {
                fraction <= 1 -> 1.0
                fraction <= 2 -> 2.0
                fraction <= 5 -> 5.0
                else -> 10.0
            }
        }
        return niceFraction * 10.0.pow(exponent)
    }
    val step = nice(nice(high - low, false) / count, true)
    val start = floor(low / step) * step
    val end = ceil(high / step) * step
    val ticks = mutableListOf<Double>()
    var value = start
    while (value <= end + step / 2 && ticks.size < 50) {
        ticks += (value / step).roundToInt() * step
        value += step
    }
    return ticks.ifEmpty { listOf(low, high) }
}

/** Compact axis label: `1.2k`, `3M`, `0.25`. */
internal fun axisLabel(value: Double): String {
    val magnitude = abs(value)
    fun trim(number: Double): String {
        val rounded = (number * 100).roundToInt() / 100.0
        return JS.numberToString(rounded)
    }
    return when {
        magnitude >= 1e9 -> trim(value / 1e9) + "B"
        magnitude >= 1e6 -> trim(value / 1e6) + "M"
        magnitude >= 1e4 -> trim(value / 1e3) + "k"
        else -> trim(value)
    }
}

private fun Double.orZero(): Double = if (isFinite()) this else 0.0

// Frame, legend and animation ---------------------------------------------------------------------

@Composable
private fun rememberEntrance(): Animatable<Float, *> {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
    return progress
}

@Composable
private fun ChartFrame(height: Dp, xLabel: String?, yLabel: String?, content: @Composable () -> Unit) {
    val theme = OpenUITheme.current
    val caption = theme.typography.caption.copy(fontSize = 11.sp, color = theme.colors.textSecondary)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.xs)) {
        if (!yLabel.isNullOrEmpty()) Text(yLabel, style = caption)
        Box(Modifier.fillMaxWidth().height(height)) { content() }
        if (!xLabel.isNullOrEmpty()) {
            Text(xLabel, style = caption, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeriesLegend(names: List<String>) {
    if (names.size < 2) return
    val theme = OpenUITheme.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(theme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(theme.spacing.xs),
    ) {
        names.forEachIndexed { index, name ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(theme.colors.chartColor(index), CircleShape))
                Spacer(Modifier.width(4.dp))
                Text(name, style = theme.typography.caption, color = theme.colors.textSecondary)
            }
        }
    }
}

/** Slices with their values (and shares). */
@Composable
private fun SliceLegend(entries: List<ChartSlice>, showPercent: Boolean) {
    val theme = OpenUITheme.current
    val total = entries.sumOf { it.value }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        entries.forEachIndexed { index, entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(theme.spacing.s),
            ) {
                Box(Modifier.size(10.dp).background(theme.colors.chartColor(index), RoundedCornerShape(3.dp)))
                Text(
                    entry.label,
                    style = theme.typography.caption,
                    color = theme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    NumberFormatting.decimal(entry.value),
                    style = theme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.colors.textPrimary,
                )
                if (showPercent && total > 0) {
                    Text(
                        NumberFormatting.percent((entry.value / total * 100).roundToInt().toDouble()),
                        style = theme.typography.caption,
                        color = theme.colors.textSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(min = 34.dp),
                    )
                }
            }
        }
    }
}

private fun DrawScope.label(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    x: Float,
    y: Float,
    maxWidth: Float,
    alignX: Float,
    alignY: Float,
) {
    val layout = measurer.measure(
        text,
        style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        constraints = Constraints(maxWidth = max(1, maxWidth.toInt())),
    )
    drawText(layout, topLeft = Offset(x - layout.size.width * alignX, y - layout.size.height * alignY))
}

// Cartesian charts -----------------------------------------------------------------------------------

internal enum class CartesianKind { Bar, Line, Area, HorizontalBar }

/** `BarChart`, `LineChart`, `AreaChart` and `HorizontalBarChart`. */
@Composable
private fun CartesianChart(context: ComponentRenderProps, kind: CartesianKind) {
    val props = context.props
    val (points, seriesNames) = ChartData.points(props.array("labels"), props.array("series"))
    val valid = points.filter { it.value.isFinite() }
    if (valid.isEmpty()) return
    val theme = OpenUITheme.current
    val categories = points.map { it.category }.distinct()
    val variant = props.string("variant")
    val height = chartHeight(props.number("height"))?.dp
        ?: if (kind == CartesianKind.HorizontalBar) min(max(160, categories.size * 34 + 40), 1000).dp else 220.dp
    val measurer = rememberTextMeasurer()
    val progress = rememberEntrance()
    val axisStyle = theme.typography.caption.copy(fontSize = 10.sp, color = theme.colors.textSecondary)
    val colors = seriesNames.indices.map { theme.colors.chartColor(it) }
    val gridColor = theme.colors.border

    Column(
        Modifier.fillMaxWidth().semantics { contentDescription = ChartData.summary(points, seriesNames) },
        verticalArrangement = Arrangement.spacedBy(theme.spacing.s),
    ) {
        ChartFrame(height, props.text("xLabel"), props.text("yLabel")) {
            Canvas(Modifier.fillMaxWidth().height(height)) {
                val stacked = variant == "stacked" && (kind == CartesianKind.Bar || kind == CartesianKind.HorizontalBar)
                val (low, high) = valueRange(valid, categories, stacked, includeZero = kind != CartesianKind.Line)
                val ticks = niceTicks(low, high)
                val domainLow = ticks.first()
                val domainHigh = ticks.last()
                val span = (domainHigh - domainLow).takeIf { it > 0 } ?: 1.0
                val t = progress.value
                if (kind == CartesianKind.HorizontalBar) {
                    drawHorizontalBars(
                        measurer, axisStyle, valid, categories, seriesNames, colors, gridColor, ticks,
                        domainLow, span, stacked, t,
                    )
                } else {
                    val tickLabels = ticks.map(::axisLabel)
                    val axisWidth = tickLabels.maxOf { measurer.measure(it, axisStyle).size.width } + 6.dp.toPx()
                    val labelHeight = measurer
                        .measure("Ag", axisStyle)
                        .size.height
                        .toFloat()
                    val plot = Rect(
                        axisWidth,
                        labelHeight / 2,
                        size.width - 4.dp.toPx(),
                        size.height - labelHeight - 6.dp.toPx(),
                    )
                    fun y(value: Double): Float = plot.bottom - ((value - domainLow) / span * plot.height).toFloat()
                    ticks.forEachIndexed { index, tick ->
                        val ty = y(tick)
                        drawLine(gridColor, Offset(plot.left, ty), Offset(plot.right, ty), strokeWidth = 0.5.dp.toPx())
                        label(measurer, tickLabels[index], axisStyle, plot.left - 6.dp.toPx(), ty, axisWidth, 1f, 0.5f)
                    }
                    val band = plot.width / categories.size
                    // Category labels, thinned out when they don't fit.
                    val widest = categories.maxOf { measurer.measure(it.take(14), axisStyle).size.width }.toFloat()
                    val every = max(1, ceil((widest + 8.dp.toPx()) / band).toInt())
                    categories.forEachIndexed { index, category ->
                        if (index % every == 0) {
                            label(
                                measurer,
                                category,
                                axisStyle,
                                plot.left + band * (index + 0.5f),
                                plot.bottom + 6.dp.toPx(),
                                band * every,
                                0.5f,
                                0f,
                            )
                        }
                    }
                    val zeroY = y(max(domainLow, min(0.0, domainHigh)))
                    when (kind) {
                        CartesianKind.Bar -> drawVerticalBars(
                            valid, categories, seriesNames, colors, plot, band, stacked, t, ::y, zeroY,
                        )
                        else -> clipRect(plot.left, 0f, plot.left + plot.width * t, size.height) {
                            drawLines(
                                valid, categories, seriesNames, colors, plot, band, variant,
                                kind == CartesianKind.Area, ::y, zeroY,
                            )
                        }
                    }
                }
            }
        }
        SeriesLegend(seriesNames)
    }
}

/** The value range to show: stacked bars sum per category (positives and negatives apart). */
private fun valueRange(
    points: List<ChartPoint>,
    categories: List<String>,
    stacked: Boolean,
    includeZero: Boolean,
): Pair<Double, Double> {
    var low: Double
    var high: Double
    if (stacked) {
        val sums = categories.map { category ->
            val values = points.filter { it.category == category }.map { it.value }
            values.filter { it < 0 }.sum() to values.filter { it > 0 }.sum()
        }
        low = sums.minOf { it.first }
        high = sums.maxOf { it.second }
    } else {
        low = points.minOf { it.value }
        high = points.maxOf { it.value }
    }
    if (includeZero) {
        low = min(low, 0.0)
        high = max(high, 0.0)
    } else if (high > low) {
        val pad = (high - low) * 0.1
        low = if (low >= 0 && low - pad < 0) 0.0 else low - pad
        high += pad
    }
    return low to high
}

private fun DrawScope.drawVerticalBars(
    points: List<ChartPoint>,
    categories: List<String>,
    series: List<String>,
    colors: List<Color>,
    plot: Rect,
    band: Float,
    stacked: Boolean,
    progress: Float,
    y: (Double) -> Float,
    zeroY: Float,
) {
    val corner = CornerRadius(3.dp.toPx())
    categories.forEachIndexed { categoryIndex, category ->
        val bandLeft = plot.left + band * categoryIndex
        val inCategory = points.filter { it.category == category }
        if (stacked) {
            val width = band * 0.6f
            val left = bandLeft + (band - width) / 2
            var positive = 0.0
            var negative = 0.0
            for (point in inCategory) {
                val base = if (point.value >= 0) positive else negative
                val top = base + point.value * progress
                if (point.value >= 0) positive = top else negative = top
                val y1 = y(base)
                val y2 = y(top)
                drawRoundRect(
                    colors[series.indexOf(point.series).coerceAtLeast(0)],
                    Offset(left, min(y1, y2)),
                    Size(width, abs(y2 - y1)),
                    corner,
                )
            }
        } else {
            val groupWidth = band * 0.7f
            val barWidth = groupWidth / max(1, series.size)
            val gap = min(2.dp.toPx(), barWidth * 0.15f)
            for (point in inCategory) {
                val seriesIndex = series.indexOf(point.series).coerceAtLeast(0)
                val left = bandLeft + (band - groupWidth) / 2 + barWidth * seriesIndex + gap / 2
                val top = y(point.value * progress)
                drawRoundRect(
                    colors[seriesIndex],
                    Offset(left, min(top, zeroY)),
                    Size(barWidth - gap, abs(zeroY - top)),
                    corner,
                )
            }
        }
    }
}

private fun DrawScope.drawLines(
    points: List<ChartPoint>,
    categories: List<String>,
    series: List<String>,
    colors: List<Color>,
    plot: Rect,
    band: Float,
    variant: String?,
    area: Boolean,
    y: (Double) -> Float,
    zeroY: Float,
) {
    series.forEachIndexed { seriesIndex, name ->
        val offsets = points
            .filter { it.series == name }
            .map { Offset(plot.left + band * (categories.indexOf(it.category) + 0.5f), y(it.value)) }
            .sortedBy { it.x }
        if (offsets.isEmpty()) return@forEachIndexed
        val color = colors[seriesIndex]
        val line = linePath(offsets, variant)
        if (area && offsets.size > 1) {
            val fill = linePath(offsets, variant).apply {
                lineTo(offsets.last().x, zeroY)
                lineTo(offsets.first().x, zeroY)
                close()
            }
            drawPath(fill, color.copy(alpha = 0.25f))
        }
        drawPath(line, color, style = Stroke(width = (if (area) 2f else 2.5f).dp.toPx(), cap = StrokeCap.Round))
        if (!area) offsets.forEach { drawCircle(color, radius = 2.8.dp.toPx(), center = it) }
    }
}

/** A line through [points]: straight, smooth (Catmull-Rom as cubic Béziers) or stepped. */
private fun linePath(points: List<Offset>, variant: String?): Path = Path().apply {
    moveTo(points.first().x, points.first().y)
    for (index in 1 until points.size) {
        val previous = points[index - 1]
        val current = points[index]
        when (variant) {
            "natural" -> {
                val before = points.getOrElse(index - 2) { previous }
                val after = points.getOrElse(index + 1) { current }
                cubicTo(
                    previous.x + (current.x - before.x) / 6,
                    previous.y + (current.y - before.y) / 6,
                    current.x - (after.x - previous.x) / 6,
                    current.y - (after.y - previous.y) / 6,
                    current.x,
                    current.y,
                )
            }
            "step" -> {
                val middle = (previous.x + current.x) / 2
                lineTo(middle, previous.y)
                lineTo(middle, current.y)
                lineTo(current.x, current.y)
            }
            else -> lineTo(current.x, current.y)
        }
    }
}

private fun DrawScope.drawHorizontalBars(
    measurer: TextMeasurer,
    axisStyle: TextStyle,
    points: List<ChartPoint>,
    categories: List<String>,
    series: List<String>,
    colors: List<Color>,
    gridColor: Color,
    ticks: List<Double>,
    domainLow: Double,
    span: Double,
    stacked: Boolean,
    progress: Float,
) {
    val tickLabels = ticks.map(::axisLabel)
    val labelHeight = measurer
        .measure("Ag", axisStyle)
        .size.height
        .toFloat()
    val categoryWidth =
        min(size.width * 0.4f, categories.maxOf { measurer.measure(it, axisStyle).size.width }.toFloat()) + 8.dp.toPx()
    val lastTickWidth = measurer.measure(tickLabels.last(), axisStyle).size.width / 2f
    val plot =
        Rect(categoryWidth, 0f, size.width - lastTickWidth - 2.dp.toPx(), size.height - labelHeight - 6.dp.toPx())
    fun x(value: Double): Float = plot.left + ((value - domainLow) / span * plot.width).toFloat()
    ticks.forEachIndexed { index, tick ->
        val tx = x(tick)
        drawLine(gridColor, Offset(tx, plot.top), Offset(tx, plot.bottom), strokeWidth = 0.5.dp.toPx())
        label(measurer, tickLabels[index], axisStyle, tx, plot.bottom + 6.dp.toPx(), size.width, 0.5f, 0f)
    }
    val band = plot.height / categories.size
    val zeroX = x(max(domainLow, min(0.0, domainLow + span)))
    val corner = CornerRadius(3.dp.toPx())
    categories.forEachIndexed { categoryIndex, category ->
        val bandTop = plot.top + band * categoryIndex
        label(
            measurer,
            category,
            axisStyle,
            categoryWidth - 8.dp.toPx(),
            bandTop + band / 2,
            categoryWidth - 8.dp.toPx(),
            1f,
            0.5f,
        )
        val inCategory = points.filter { it.category == category }
        if (stacked) {
            val thickness = band * 0.6f
            val top = bandTop + (band - thickness) / 2
            var positive = 0.0
            var negative = 0.0
            for (point in inCategory) {
                val base = if (point.value >= 0) positive else negative
                val end = base + point.value * progress
                if (point.value >= 0) positive = end else negative = end
                val x1 = x(base)
                val x2 = x(end)
                drawRoundRect(
                    colors[series.indexOf(point.series).coerceAtLeast(0)],
                    Offset(min(x1, x2), top),
                    Size(abs(x2 - x1), thickness),
                    corner,
                )
            }
        } else {
            val groupHeight = band * 0.7f
            val barHeight = groupHeight / max(1, series.size)
            val gap = min(2.dp.toPx(), barHeight * 0.15f)
            for (point in inCategory) {
                val seriesIndex = series.indexOf(point.series).coerceAtLeast(0)
                val top = bandTop + (band - groupHeight) / 2 + barHeight * seriesIndex + gap / 2
                val end = x(point.value * progress)
                drawRoundRect(
                    colors[seriesIndex],
                    Offset(min(zeroX, end), top),
                    Size(abs(end - zeroX), barHeight - gap),
                    corner,
                )
            }
        }
    }
}

// Scatter --------------------------------------------------------------------------------------

private data class ScatterPoint(val series: Int, val x: Double, val y: Double, val z: Double?)

/** `ScatterChart(datasets, xLabel?, yLabel?)` with `ScatterSeries(name, points)` and `Point(x, y, z?)`. */
@Composable
private fun ScatterChart(context: ComponentRenderProps) {
    val names = mutableListOf<String>()
    val points = mutableListOf<ScatterPoint>()
    for (dataset in context.props.array("datasets")) {
        val props = dataset.elementValue?.props ?: dataset.objectValue ?: OpenUIObject.Empty
        names += props["name"]?.let(JS::toString).orEmpty()
        for (point in props["points"].arrayValue.orEmpty()) {
            val pointProps = point.elementValue?.props ?: point.objectValue ?: OpenUIObject.Empty
            val x = JS.toNumber(pointProps["x"])
            val y = JS.toNumber(pointProps["y"])
            if (!x.isFinite() || !y.isFinite()) continue
            points +=
                ScatterPoint(
                    names.lastIndex,
                    x,
                    y,
                    pointProps["z"]
                        .takeUnless(JS::isNullish)?.let(JS::toNumber)
                        ?.takeIf {
                            it.isFinite()
                        },
                )
        }
    }
    if (points.isEmpty()) return
    val theme = OpenUITheme.current
    val measurer = rememberTextMeasurer()
    val progress = rememberEntrance()
    val axisStyle = theme.typography.caption.copy(fontSize = 10.sp, color = theme.colors.textSecondary)
    val gridColor = theme.colors.border
    val colors = names.indices.map { theme.colors.chartColor(it) }
    val maxZ = max(points.mapNotNull { it.z }.maxOrNull() ?: 1.0, 1.0)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(theme.spacing.s)) {
        ChartFrame(240.dp, context.props.text("xLabel"), context.props.text("yLabel")) {
            Canvas(Modifier.fillMaxWidth().height(240.dp)) {
                fun padded(values: List<Double>): Pair<Double, Double> {
                    val low = values.min()
                    val high = values.max()
                    val pad = if (high > low) (high - low) * 0.08 else 1.0
                    return (if (low >= 0 && low - pad < 0) 0.0 else low - pad) to high + pad
                }
                val xRange = padded(points.map { it.x })
                val yRange = padded(points.map { it.y })
                val xTicks = niceTicks(xRange.first, xRange.second)
                val yTicks = niceTicks(yRange.first, yRange.second)
                val yLabels = yTicks.map(::axisLabel)
                val axisWidth = yLabels.maxOf { measurer.measure(it, axisStyle).size.width } + 6.dp.toPx()
                val labelHeight = measurer
                    .measure("Ag", axisStyle)
                    .size.height
                    .toFloat()
                val lastXWidth = measurer.measure(axisLabel(xTicks.last()), axisStyle).size.width / 2f
                val plot = Rect(
                    axisWidth,
                    labelHeight / 2,
                    size.width - lastXWidth - 2.dp.toPx(),
                    size.height - labelHeight - 6.dp.toPx(),
                )
                val xSpan = (xTicks.last() - xTicks.first()).takeIf { it > 0 } ?: 1.0
                val ySpan = (yTicks.last() - yTicks.first()).takeIf { it > 0 } ?: 1.0
                fun px(value: Double) = plot.left + ((value - xTicks.first()) / xSpan * plot.width).toFloat()
                fun py(value: Double) = plot.bottom - ((value - yTicks.first()) / ySpan * plot.height).toFloat()
                yTicks.forEachIndexed { index, tick ->
                    drawLine(
                        gridColor,
                        Offset(plot.left, py(tick)),
                        Offset(plot.right, py(tick)),
                        strokeWidth = 0.5.dp.toPx(),
                    )
                    label(measurer, yLabels[index], axisStyle, plot.left - 6.dp.toPx(), py(tick), axisWidth, 1f, 0.5f)
                }
                xTicks.forEach { tick ->
                    drawLine(
                        gridColor,
                        Offset(px(tick), plot.top),
                        Offset(px(tick), plot.bottom),
                        strokeWidth = 0.5.dp.toPx(),
                    )
                    label(
                        measurer,
                        axisLabel(tick),
                        axisStyle,
                        px(tick),
                        plot.bottom + 6.dp.toPx(),
                        size.width,
                        0.5f,
                        0f,
                    )
                }
                for (point in points) {
                    // The bubble area grows with z, from 30 to 200 dp².
                    val area = point.z?.let { 30 + 170 * it / maxZ } ?: 50.0
                    val radius = (sqrt(area / Math.PI).toFloat() * progress.value).dp.toPx()
                    drawCircle(colors[point.series].copy(alpha = 0.8f), radius, Offset(px(point.x), py(point.y)))
                }
            }
        }
        SeriesLegend(names)
    }
}

// Proportional charts -------------------------------------------------------------------------------

private fun sectorPath(rect: Rect, start: Float, sweep: Float, innerRatio: Float): Path = Path().apply {
    val center = rect.center
    if (innerRatio > 0f) {
        arcTo(rect, start, sweep, forceMoveTo = true)
        val inner = Rect(center, rect.width / 2 * innerRatio)
        arcTo(inner, start + sweep, -sweep, forceMoveTo = false)
    } else {
        moveTo(center.x, center.y)
        arcTo(rect, start, sweep, forceMoveTo = false)
    }
    close()
}

/** `PieChart(labels, values, variant?, appearance?)`. */
@Composable
private fun PieChart(context: ComponentRenderProps) {
    val slices = ChartData.slices(context.props.array("labels"), context.props.array("values"))
    val total = slices.sumOf { max(0.0, it.value.orZero()) }
    if (total <= 0) return
    val theme = OpenUITheme.current
    val donut = context.props.string("variant") == "donut"
    val semicircle = context.props.string("appearance") == "semiCircular"
    val span = if (semicircle) 180f else 360f
    val startOffset = if (semicircle) 180f else -90f
    val progress = rememberEntrance()
    val separator = theme.colors.surface
    val colors = slices.indices.map { theme.colors.chartColor(it) }

    Column(
        Modifier.fillMaxWidth().semantics {
            contentDescription =
                slices.joinToString(", ") { "${it.label} ${NumberFormatting.decimal(it.value)}" }
        },
        verticalArrangement = Arrangement.spacedBy(theme.spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(if (semicircle) 220.dp else 180.dp, if (semicircle) 110.dp else 180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(if (semicircle) 220.dp else 180.dp, if (semicircle) 110.dp else 180.dp)) {
                val radius = if (semicircle) min(size.width / 2, size.height) else min(size.width, size.height) / 2
                val center = if (semicircle) {
                    Offset(size.width / 2, size.height)
                } else {
                    Offset(size.width / 2, size.height / 2)
                }
                val rect = Rect(center, radius)
                var before = 0.0
                slices.forEachIndexed { index, slice ->
                    val value = max(0.0, slice.value.orZero())
                    val start = startOffset + (before / total * span).toFloat() * progress.value
                    val sweep = (value / total * span).toFloat() * progress.value
                    before += value
                    if (sweep <= 0f) return@forEachIndexed
                    val path = sectorPath(rect, start, sweep, if (donut) 0.58f else 0f)
                    drawPath(path, colors[index])
                    drawPath(path, separator, style = Stroke(1.5.dp.toPx()))
                }
            }
            if (donut) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = if (semicircle) Modifier.align(Alignment.BottomCenter) else Modifier,
                ) {
                    Text(
                        NumberFormatting.decimal((total * 100).roundToInt() / 100.0),
                        style = theme.typography.heading,
                        color = theme.colors.textPrimary,
                    )
                    Text(
                        stringResource(R.string.stream_ai_openui_chart_total),
                        style = theme.typography.caption.copy(fontSize = 11.sp),
                        color = theme.colors.textSecondary,
                    )
                }
            }
        }
        SliceLegend(slices, showPercent = true)
    }
}

/** `RadialChart(labels, values)`: one ring per category. */
@Composable
private fun RadialChart(context: ComponentRenderProps) {
    val slices = ChartData.slices(context.props.array("labels"), context.props.array("values"))
    val maximum = slices.maxOfOrNull { it.value.orZero() } ?: 0.0
    if (slices.isEmpty() || maximum <= 0) return
    val theme = OpenUITheme.current
    val progress = rememberEntrance()
    val track = theme.colors.sunkenSurface
    val colors = slices.indices.map { theme.colors.chartColor(it) }

    Column(
        Modifier.fillMaxWidth().semantics {
            contentDescription =
                slices.joinToString(", ") { "${it.label} ${NumberFormatting.decimal(it.value)}" }
        },
        verticalArrangement = Arrangement.spacedBy(theme.spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.size(180.dp)) {
            val stroke = 11.dp.toPx()
            slices.forEachIndexed { index, slice ->
                val inset = index * 16.dp.toPx() + stroke / 2
                if (inset * 2 >= size.minDimension) return@forEachIndexed
                val topLeft = Offset(inset, inset)
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                val sweep = (max(0.0, slice.value.orZero()) / maximum * 0.75 * 360).toFloat() * progress.value
                drawArc(
                    colors[index],
                    -90f,
                    sweep,
                    false,
                    topLeft,
                    arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        SliceLegend(slices, showPercent = false)
    }
}

/** `SingleStackedBarChart(labels, values)`: one horizontal bar split by share. */
@Composable
private fun SingleStackedBarChart(context: ComponentRenderProps) {
    val slices = ChartData.slices(context.props.array("labels"), context.props.array("values"))
    val total = slices.sumOf { max(0.0, it.value.orZero()) }
    if (total <= 0) return
    val theme = OpenUITheme.current
    val progress = rememberEntrance()
    val colors = slices.indices.map { theme.colors.chartColor(it) }

    Column(
        Modifier.fillMaxWidth().semantics {
            contentDescription =
                slices.joinToString(", ") { "${it.label} ${NumberFormatting.decimal(it.value)}" }
        },
        verticalArrangement = Arrangement.spacedBy(theme.spacing.m),
    ) {
        Canvas(Modifier.fillMaxWidth().height(16.dp)) {
            val gap = 2.dp.toPx()
            val available = (size.width - gap * (slices.size - 1)).coerceAtLeast(0f) * progress.value
            val clip = Path().apply {
                addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(size.height / 2)))
            }
            clipPath(clip) {
                var x = 0f
                slices.forEachIndexed { index, slice ->
                    val width = (available * max(0.0, slice.value.orZero()) / total).toFloat()
                    drawRect(colors[index], Offset(x, 0f), Size(width, size.height))
                    x += width + gap
                }
            }
        }
        SliceLegend(slices, showPercent = true)
    }
}

/** `RadarChart(labels, series)`. */
@Composable
private fun RadarChart(context: ComponentRenderProps) {
    val labels = context.props.strings("labels")
    val (points, seriesNames) = ChartData.points(context.props.array("labels"), context.props.array("series"))
    val valid = points.filter { it.value.isFinite() }
    if (labels.size < 3 || valid.isEmpty()) return
    val theme = OpenUITheme.current
    val maximum = max(valid.maxOf { it.value }, 1.0)
    val measurer = rememberTextMeasurer()
    val progress = rememberEntrance()
    val labelStyle = theme.typography.caption.copy(fontSize = 11.sp, color = theme.colors.textSecondary)
    val gridColor = theme.colors.border
    val colors = seriesNames.indices.map { theme.colors.chartColor(it) }

    Column(
        Modifier.fillMaxWidth().semantics { contentDescription = ChartData.summary(points, seriesNames) },
        verticalArrangement = Arrangement.spacedBy(theme.spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.fillMaxWidth().height(250.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 2 - 28.dp.toPx()
            fun point(index: Int, fraction: Double): Offset {
                val angle = -Math.PI / 2 + index.toDouble() / labels.size * 2 * Math.PI
                return Offset(
                    center.x + (cos(angle) * fraction).toFloat() * radius,
                    center.y + (sin(angle) * fraction).toFloat() * radius,
                )
            }
            for (level in 1..4) {
                val grid = Path()
                labels.indices.forEach { index ->
                    val p = point(index, level / 4.0)
                    if (index == 0) grid.moveTo(p.x, p.y) else grid.lineTo(p.x, p.y)
                }
                grid.close()
                drawPath(grid, gridColor, style = Stroke(0.5.dp.toPx()))
            }
            labels.forEachIndexed { index, text ->
                drawLine(gridColor, center, point(index, 1.0), strokeWidth = 0.5.dp.toPx())
                val at = point(index, 1.18)
                label(measurer, text, labelStyle, at.x, at.y, size.width / 3, 0.5f, 0.5f)
            }
            seriesNames.forEachIndexed { seriesIndex, name ->
                val values = valid.filter { it.series == name }.sortedBy { it.order }
                if (values.isEmpty()) return@forEachIndexed
                val shape = Path()
                values.forEachIndexed { index, value ->
                    val p = point(value.order, max(0.0, value.value) / maximum * progress.value)
                    if (index == 0) shape.moveTo(p.x, p.y) else shape.lineTo(p.x, p.y)
                }
                shape.close()
                drawPath(shape, colors[seriesIndex].copy(alpha = 0.2f))
                drawPath(shape, colors[seriesIndex], style = Stroke(2.dp.toPx()))
            }
        }
        SeriesLegend(seriesNames)
    }
}
