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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.ripple
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds drag pointer input to the modifier.
 */
internal fun Modifier.dragPointerInput(
    enabled: Boolean = true,
    onDragStart: (Offset) -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragStop: (Offset?) -> Unit = {},
): Modifier {
    if (enabled.not()) {
        return this
    }
    return this
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { onDrag(it) },
                onDrag = { change, _ ->
                    change.consume()
                    onDrag(change.position)
                },
                onDragEnd = { onDragStop(null) },
                onDragCancel = { onDragStop(null) },
            )
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { onDragStart(it) },
                onTap = { onDragStop(it) },
            )
        }
}

/**
 * Add this modifier to the element to make it clickable within its bounds
 * and show a default indication when it's pressed.
 *
 * @param bounded If `true`, ripples are clipped by the bounds of the target layout.
 * Unbounded ripples always animate from the target layout center, bounded ripples animate from the touch position.
 * @param enabled Controls the enabled state.
 * When `false`, [onClick], and this modifier will appear disabled for accessibility service.
 * @param onClick The callback to be invoked when the click gesture is detected.
 */
internal fun Modifier.clickable(
    bounded: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier =
    clickable(
        interactionSource = null,
        indication = ripple(bounded),
        enabled = enabled,
        onClick = onClick,
    )

internal inline fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier) =
    if (condition) this.block() else this

internal inline fun <T : Any> Modifier.ifNotNull(value: T?, block: Modifier.(T) -> Modifier) =
    if (value != null) this.block(value) else this

internal fun Modifier.bottomBorder(color: Color, width: Dp = 1.dp): Modifier =
    verticalBorder(color, width, yPosition = { size.height - it / 2 })

internal fun Modifier.topBorder(color: Color, width: Dp = 1.dp): Modifier =
    verticalBorder(color = color, width = width, yPosition = { it / 2 })

/**
 * Draws a full-width border at the position returned by [yPosition].
 *
 * @param color The color of the border.
 * @param width The width of the border.
 * @param yPosition A lambda that calculates the y position of the border based on the width in pixels.
 */
private inline fun Modifier.verticalBorder(
    color: Color,
    width: Dp,
    crossinline yPosition: ContentDrawScope.(widthPx: Float) -> Float,
): Modifier = drawWithContent {
    drawContent()
    val widthPx = width.toPx()
    val y = yPosition(widthPx)
    drawLine(color = color, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = widthPx)
}
