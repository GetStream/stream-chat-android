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

@file:Suppress("MatchingDeclarationName") // File groups related gesture types, not just the config class.

package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import kotlin.math.abs

/** Horizontal drag distance at which the recording is cancelled. */
internal val SlideToCancelThreshold = 96.dp

/**
 * Thresholds used to interpret drag gestures during audio recording.
 *
 * @property cancelThresholdPx Horizontal drag distance (px) to cancel recording.
 * @property lockThresholdPx Vertical drag distance (px) to lock recording.
 * @property isRtl `true` when the layout direction is right-to-left. In RTL the raw
 *  screen-coordinate drag is normalised so that cancel is always negative-x, matching
 *  the convention expected by [evaluateDragThreshold] and the downstream UI code.
 */
internal class RecordingGestureConfig(
    val cancelThresholdPx: Float,
    val lockThresholdPx: Float,
    val isRtl: Boolean,
)

@VisibleForTesting
internal enum class DragAxis { Horizontal, Vertical }

/**
 * Terminal outcome of the recording drag gesture.
 */
@VisibleForTesting
internal enum class DragResult {
    /** Finger lifted — caller should complete the recording if not already locked. */
    Released,

    /** Dragged past the cancel threshold. */
    Cancel,

    /** Dragged past the lock threshold. */
    Lock,

    /** State was already [RecordingState.Locked] (e.g. from a prior frame). */
    AlreadyLocked,
}

/**
 * Handles the full gesture lifecycle:
 * 1. Waits for the platform long-press timeout — if the user releases before that,
 *    it's a tap → [onShowHint].
 * 2. Once the threshold is reached, starts recording and tracks drag for cancel / lock / confirm.
 *
 * Drag is axis-locked: once the first significant movement picks a direction
 * (left → cancel, up → lock), the offset is constrained to that axis.
 */
internal suspend fun AwaitPointerEventScope.handleRecordingGesture(
    down: PointerInputChange,
    config: RecordingGestureConfig,
    currentState: () -> RecordingState,
    recordingActions: AudioRecordingActions,
    onShowHint: () -> Unit,
) {
    if (!awaitHoldThreshold(down)) {
        onShowHint()
        return
    }

    recordingActions.onStartRecording()

    val result = awaitDragResult(down, config, currentState, recordingActions.onHoldRecording)
    handleDragResult(result, currentState, recordingActions)
}

/**
 * Waits for the platform long-press timeout. Returns `true` if the user held long enough,
 * `false` if they released early (tap).
 */
private suspend fun AwaitPointerEventScope.awaitHoldThreshold(
    down: PointerInputChange,
): Boolean {
    val releasedEarly = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
        while (true) {
            val event = awaitDragOrCancellation(down.id) ?: return@withTimeoutOrNull
            if (!event.pressed) return@withTimeoutOrNull
            event.consume()
        }
    } != null
    return !releasedEarly
}

/**
 * Tracks drag events after recording has started, returning a [DragResult]
 * when a terminal condition is reached (release, cancel threshold, lock threshold,
 * or externally locked state).
 */
private suspend fun AwaitPointerEventScope.awaitDragResult(
    down: PointerInputChange,
    config: RecordingGestureConfig,
    currentState: () -> RecordingState,
    onDragOffset: (Offset) -> Unit,
): DragResult {
    val startPosition = down.position
    var dragAxis: DragAxis? = null
    val touchSlop = viewConfiguration.touchSlop

    while (true) {
        if (currentState() is RecordingState.Locked) return DragResult.AlreadyLocked

        val dragEvent = awaitDragOrCancellation(down.id)
        if (dragEvent == null || !dragEvent.pressed) return DragResult.Released

        dragEvent.consume()
        val rawDiff = dragEvent.position.minus(startPosition)
        dragAxis = dragAxis ?: resolveAxis(rawDiff, touchSlop)

        val constrained = constrainToAxis(rawDiff, dragAxis)
        val logical = if (config.isRtl) constrained.copy(x = -constrained.x) else constrained
        onDragOffset(logical)

        evaluateDragThreshold(logical, config)?.let { return it }
    }
}

/**
 * Determines the drag axis once the first significant movement exceeds [touchSlop].
 * Returns `null` if the movement is still too small.
 */
@VisibleForTesting
internal fun resolveAxis(rawDiff: Offset, touchSlop: Float): DragAxis? {
    val absX = abs(rawDiff.x)
    val absY = abs(rawDiff.y)
    if (absX <= touchSlop && absY <= touchSlop) return null
    return if (absX > absY) DragAxis.Horizontal else DragAxis.Vertical
}

/**
 * Constrains a raw offset to the given [axis]. Returns [Offset.Zero] if no axis is locked yet.
 */
@VisibleForTesting
internal fun constrainToAxis(rawDiff: Offset, axis: DragAxis?): Offset = when (axis) {
    DragAxis.Horizontal -> Offset(rawDiff.x, 0f)
    DragAxis.Vertical -> Offset(0f, rawDiff.y)
    null -> Offset.Zero
}

/**
 * Returns a terminal [DragResult] when the constrained offset crosses a gesture threshold,
 * or `null` if the drag is still within bounds.
 *
 * The offset is expected in **logical coordinates** where cancel is always negative-x.
 * RTL normalisation (negating x for right-to-left layouts) happens upstream in [awaitDragResult].
 *
 * Cancel (horizontal) is evaluated before lock (vertical), so a simultaneous breach favours cancel.
 */
@VisibleForTesting
internal fun evaluateDragThreshold(
    constrained: Offset,
    config: RecordingGestureConfig,
): DragResult? = when {
    constrained.x <= -config.cancelThresholdPx -> DragResult.Cancel
    constrained.y <= -config.lockThresholdPx -> DragResult.Lock
    else -> null
}

/**
 * Dispatches the appropriate recording action for the given [result].
 *
 * On [DragResult.Released] the recording is confirmed only when the current state is **not**
 * already [RecordingState.Locked] (the lock UI handles its own send flow).
 */
@VisibleForTesting
internal fun handleDragResult(
    result: DragResult,
    currentState: () -> RecordingState,
    recordingActions: AudioRecordingActions,
) {
    when (result) {
        DragResult.Released -> {
            if (currentState() !is RecordingState.Locked) recordingActions.onConfirmRecording()
        }
        DragResult.Cancel -> recordingActions.onCancelRecording()
        DragResult.Lock -> recordingActions.onLockRecording()
        DragResult.AlreadyLocked -> Unit
    }
}
