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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.util.internal.AIImageLoader
import io.getstream.chat.android.ai.openui.icons.Image
import io.getstream.chat.android.ai.openui.rendering.OpenUITheme
import io.getstream.chat.android.ai.openui.utils.OpenUILink

// Surfaces ------------------------------------------------------------------------------------

/** Container surfaces used by `Card`, `MarkDownRenderer`, `SwitchGroup`, ... (`card` | `sunk` | `clear`). */
@Composable
internal fun Modifier.openUISurface(variant: String?, padding: Dp? = null): Modifier {
    val theme = OpenUITheme.current
    return when (variant) {
        "clear" -> this
        "sunk" ->
            this
                .background(theme.colors.sunkenSurface, RoundedCornerShape(theme.radius.medium))
                .padding(padding ?: theme.spacing.l)
        else ->
            this
                .clip(RoundedCornerShape(theme.radius.large))
                .background(theme.colors.surface)
                .border(0.5.dp, theme.colors.border.copy(alpha = 0.6f), RoundedCornerShape(theme.radius.large))
                .padding(padding ?: theme.spacing.l)
    }
}

/** A rounded, tinted background for semantic variants. */
internal fun Modifier.tinted(color: Color, cornerRadius: Dp, alpha: Float = 0.12f): Modifier =
    background(color.copy(alpha = alpha), RoundedCornerShape(cornerRadius))

// Images --------------------------------------------------------------------------------------

/** The size an image loads at when the space it shows in has no bound, in pixels. */
private const val UnboundedImageSizePx = 1200

/** [url] when it is a web link an image can load from. */
private fun loadableImageUrl(url: String?): String? =
    url?.takeIf { OpenUILink.isOpenable(it) && AIImageLoader.isLoadable(it) }

private sealed interface ImageState {
    data object Loading : ImageState
    data object Failed : ImageState
    data class Loaded(val bitmap: ImageBitmap) : ImageState
}

/**
 * A remote image with a neutral placeholder and a failure state; sized by its [modifier]. The model
 * writes the link, so the image downloads with [AIImageLoader], only from a public host, at about
 * the size it shows at.
 */
@Composable
internal fun RemoteImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alt: String? = null,
) {
    BoxWithConstraints(modifier) {
        val sizePx = listOf(constraints.maxWidth, constraints.maxHeight)
            .filter { it != Constraints.Infinity }
            .maxOrNull()
            ?.takeIf { it > 0 }
            ?: UnboundedImageSizePx
        val state = rememberImageState(loadableImageUrl(url), sizePx)
        RemoteImageContent(state, Modifier.fillMaxSize(), contentScale, alt)
    }
}

@Composable
private fun rememberImageState(url: String?, sizePx: Int): ImageState {
    val preview = LocalInspectionMode.current
    val state by produceState(
        url?.let { AIImageLoader.cached(it, sizePx) }?.let { ImageState.Loaded(it.asImageBitmap()) }
            ?: ImageState.Loading,
        url,
        sizePx,
    ) {
        if (value is ImageState.Loaded || preview) return@produceState
        value = url?.let { AIImageLoader.load(it, sizePx) }
            ?.let { ImageState.Loaded(it.asImageBitmap()) }
            ?: ImageState.Failed
    }
    return if (url == null) ImageState.Failed else state
}

@Composable
private fun RemoteImageContent(state: ImageState, modifier: Modifier, contentScale: ContentScale, alt: String?) {
    val theme = OpenUITheme.current
    when (state) {
        is ImageState.Loaded -> Image(
            state.bitmap,
            contentDescription = alt,
            modifier = modifier,
            contentScale = contentScale,
        )
        else -> Box(modifier.background(theme.colors.sunkenSurface), contentAlignment = Alignment.Center) {
            if (state is ImageState.Loading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Rounded.Image, contentDescription = null, tint = theme.colors.textTertiary)
                    if (!alt.isNullOrEmpty()) {
                        Text(
                            alt,
                            style = theme.typography.caption,
                            color = theme.colors.textTertiary,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// Layouts -------------------------------------------------------------------------------------

/** A responsive grid: as many equal-width columns as fit [minItemWidth], at most [maxColumns]. */
@Composable
internal fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
    minItemWidth: Dp = 150.dp,
    maxColumns: Int = 4,
    content: @Composable () -> Unit,
) {
    Layout(content, modifier.fillMaxWidth()) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val width = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            minItemWidth.roundToPx() * 2 + spacingPx
        }
        val fitting = (width + spacingPx) / (minItemWidth.roundToPx() + spacingPx)
        val columns = maxOf(1, minOf(maxColumns, fitting, maxOf(measurables.size, 1)))
        val itemWidth = ((width - (columns - 1) * spacingPx) / columns).coerceAtLeast(0)
        val rows = measurables.chunked(columns)
        // Measured at the column width; no intrinsics, which lazy and subcomposed children don't support.
        val placeables = rows.map { row ->
            row.map {
                it.measure(Constraints(minWidth = itemWidth, maxWidth = itemWidth))
            }
        }
        val totalHeight =
            placeables.sumOf { row -> row.maxOfOrNull { it.height } ?: 0 } + maxOf(0, rows.size - 1) * spacingPx
        layout(width, totalHeight) {
            var y = 0
            placeables.forEach { row ->
                row.forEachIndexed { column, placeable -> placeable.placeRelative(column * (itemWidth + spacingPx), y) }
                y += (row.maxOfOrNull { it.height } ?: 0) + spacingPx
            }
        }
    }
}

/** A row whose children share the width equally (cards in a row). */
@Composable
internal fun EqualWidthRow(
    modifier: Modifier = Modifier,
    spacing: Dp,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable () -> Unit,
) {
    Layout(content, modifier.fillMaxWidth()) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val count = maxOf(measurables.size, 1)
        val width = constraints.maxWidth
        val itemWidth = ((width - (count - 1) * spacingPx) / count).coerceAtLeast(0)
        val placeables = measurables.map { it.measure(Constraints(minWidth = itemWidth, maxWidth = itemWidth)) }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val y = verticalAlignment.align(placeable.height, height)
                placeable.placeRelative(index * (itemWidth + spacingPx), y)
            }
        }
    }
}

/** Flexbox-like container for `Stack` and the standard `Card` (direction, gap, align, justify, wrap). */
// Each parameter is one flexbox property of `Stack` and the standard `Card`.
@Suppress("LongParameterList")
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FlexContainer(
    direction: String?,
    gap: Dp,
    align: String?,
    justify: String?,
    wrap: Boolean,
    /** Children that share space equally in rows (cards). */
    flexibleChildren: Boolean,
    itemCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (direction == "row") {
        when {
            wrap && flexibleChildren -> ResponsiveGrid(
                modifier,
                spacing = gap,
                minItemWidth = if (itemCount > 2) {
                    140.dp
                } else {
                    160.dp
                },
                content = content,
            )
            wrap -> FlowRow(
                modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap, justification(justify)),
                verticalArrangement = Arrangement.spacedBy(gap),
            ) { content() }
            flexibleChildren && (justify == null || justify == "start") -> EqualWidthRow(
                modifier,
                gap,
                // Unset and `stretch` keep the cards top-aligned.
                verticalAlignment = if (align == "center" || align == "end") rowAlignment(align) else Alignment.Top,
                content = content,
            )
            else -> Row(
                modifier.fillMaxWidth(),
                horizontalArrangement = when (justify) {
                    "center" -> Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                    "end" -> Arrangement.spacedBy(gap, Alignment.End)
                    "between" -> Arrangement.SpaceBetween
                    "around" -> Arrangement.SpaceAround
                    "evenly" -> Arrangement.SpaceEvenly
                    else -> Arrangement.spacedBy(gap)
                },
                verticalAlignment = rowAlignment(align),
            ) { content() }
        }
    } else {
        Column(
            modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = when (align) {
                "center" -> Alignment.CenterHorizontally
                "end" -> Alignment.End
                else -> Alignment.Start
            },
        ) { content() }
    }
}

private fun rowAlignment(align: String?): Alignment.Vertical = when (align) {
    "start", "baseline" -> Alignment.Top
    "end" -> Alignment.Bottom
    else -> Alignment.CenterVertically
}

private fun justification(justify: String?): Alignment.Horizontal = when (justify) {
    "center" -> Alignment.CenterHorizontally
    "end" -> Alignment.End
    else -> Alignment.Start
}

/** Fills the available size (for overlays inside fixed-size boxes). */
internal val FillMax: Modifier = Modifier.fillMaxSize()
