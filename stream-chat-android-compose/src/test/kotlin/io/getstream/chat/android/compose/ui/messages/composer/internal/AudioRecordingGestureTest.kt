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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.ui.geometry.Offset
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AudioRecordingGestureTest {

    // -- resolveAxis -------------------------------------------------------

    @Nested
    inner class ResolveAxisTest {

        @Test
        fun `returns null when both axes are within touch slop`() {
            val result = resolveAxis(Offset(5f, 5f), touchSlop = 10f)
            assertNull(result)
        }

        @Test
        fun `returns null when exactly at touch slop boundary`() {
            val result = resolveAxis(Offset(10f, 10f), touchSlop = 10f)
            assertNull(result)
        }

        @Test
        fun `returns Horizontal when horizontal exceeds vertical`() {
            val result = resolveAxis(Offset(20f, 5f), touchSlop = 10f)
            assertEquals(DragAxis.Horizontal, result)
        }

        @Test
        fun `returns Vertical when vertical exceeds horizontal`() {
            val result = resolveAxis(Offset(5f, 20f), touchSlop = 10f)
            assertEquals(DragAxis.Vertical, result)
        }

        @Test
        fun `returns Vertical when both axes are equal and exceed slop`() {
            val result = resolveAxis(Offset(15f, 15f), touchSlop = 10f)
            assertEquals(DragAxis.Vertical, result)
        }

        @Test
        fun `handles negative horizontal offset`() {
            val result = resolveAxis(Offset(-20f, -5f), touchSlop = 10f)
            assertEquals(DragAxis.Horizontal, result)
        }

        @Test
        fun `handles negative vertical offset`() {
            val result = resolveAxis(Offset(-5f, -20f), touchSlop = 10f)
            assertEquals(DragAxis.Vertical, result)
        }

        @Test
        fun `returns Horizontal when only x exceeds slop`() {
            val result = resolveAxis(Offset(15f, 3f), touchSlop = 10f)
            assertEquals(DragAxis.Horizontal, result)
        }

        @Test
        fun `returns Vertical when only y exceeds slop`() {
            val result = resolveAxis(Offset(3f, 15f), touchSlop = 10f)
            assertEquals(DragAxis.Vertical, result)
        }
    }

    // -- constrainToAxis ---------------------------------------------------

    @Nested
    inner class ConstrainToAxisTest {

        @Test
        fun `returns zero offset when axis is null`() {
            val result = constrainToAxis(Offset(10f, 20f), axis = null)
            assertEquals(Offset.Zero, result)
        }

        @Test
        fun `constrains to horizontal axis zeroing y`() {
            val result = constrainToAxis(Offset(10f, 20f), DragAxis.Horizontal)
            assertEquals(Offset(10f, 0f), result)
        }

        @Test
        fun `constrains to vertical axis zeroing x`() {
            val result = constrainToAxis(Offset(10f, 20f), DragAxis.Vertical)
            assertEquals(Offset(0f, 20f), result)
        }

        @Test
        fun `preserves negative horizontal value`() {
            val result = constrainToAxis(Offset(-30f, 10f), DragAxis.Horizontal)
            assertEquals(Offset(-30f, 0f), result)
        }

        @Test
        fun `preserves negative vertical value`() {
            val result = constrainToAxis(Offset(10f, -30f), DragAxis.Vertical)
            assertEquals(Offset(0f, -30f), result)
        }
    }

    // -- evaluateDragThreshold ---------------------------------------------

    @Nested
    inner class EvaluateDragThresholdTest {

        private val config = RecordingGestureConfig(
            cancelThresholdPx = 100f,
            lockThresholdPx = 80f,
        )

        @Test
        fun `returns null when within both thresholds`() {
            val result = evaluateDragThreshold(Offset(-50f, -30f), config)
            assertNull(result)
        }

        @Test
        fun `returns Cancel when x exceeds cancel threshold`() {
            val result = evaluateDragThreshold(Offset(-150f, 0f), config)
            assertEquals(DragResult.Cancel, result)
        }

        @Test
        fun `returns Cancel at exact cancel boundary`() {
            val result = evaluateDragThreshold(Offset(-100f, 0f), config)
            assertEquals(DragResult.Cancel, result)
        }

        @Test
        fun `returns null just before cancel threshold`() {
            val result = evaluateDragThreshold(Offset(-99.9f, 0f), config)
            assertNull(result)
        }

        @Test
        fun `returns Lock when y exceeds lock threshold`() {
            val result = evaluateDragThreshold(Offset(0f, -100f), config)
            assertEquals(DragResult.Lock, result)
        }

        @Test
        fun `returns Lock at exact lock boundary`() {
            val result = evaluateDragThreshold(Offset(0f, -80f), config)
            assertEquals(DragResult.Lock, result)
        }

        @Test
        fun `returns null just before lock threshold`() {
            val result = evaluateDragThreshold(Offset(0f, -79.9f), config)
            assertNull(result)
        }

        @Test
        fun `cancel takes priority when both thresholds are exceeded`() {
            val result = evaluateDragThreshold(Offset(-150f, -100f), config)
            assertEquals(DragResult.Cancel, result)
        }

        @Test
        fun `positive offsets never trigger thresholds`() {
            val result = evaluateDragThreshold(Offset(200f, 200f), config)
            assertNull(result)
        }
    }

    // -- handleDragResult --------------------------------------------------

    @Nested
    inner class HandleDragResultTest {

        @Test
        fun `Released confirms recording when state is not Locked`() {
            var confirmed = false
            val actions = AudioRecordingActions.None.copy(
                onConfirmRecording = { confirmed = true },
            )

            handleDragResult(DragResult.Released, { RecordingState.Hold() }, actions)

            assertTrue(confirmed)
        }

        @Test
        fun `Released does not confirm when state is Locked`() {
            var confirmed = false
            val actions = AudioRecordingActions.None.copy(
                onConfirmRecording = { confirmed = true },
            )

            handleDragResult(DragResult.Released, { RecordingState.Locked() }, actions)

            assertFalse(confirmed)
        }

        @Test
        fun `Cancel invokes onCancelRecording`() {
            var cancelled = false
            val actions = AudioRecordingActions.None.copy(
                onCancelRecording = { cancelled = true },
            )

            handleDragResult(DragResult.Cancel, { RecordingState.Hold() }, actions)

            assertTrue(cancelled)
        }

        @Test
        fun `Lock invokes onLockRecording`() {
            var locked = false
            val actions = AudioRecordingActions.None.copy(
                onLockRecording = { locked = true },
            )

            handleDragResult(DragResult.Lock, { RecordingState.Hold() }, actions)

            assertTrue(locked)
        }

        @Test
        fun `AlreadyLocked does not invoke any action`() {
            var anyActionCalled = false
            val actions = AudioRecordingActions.None.copy(
                onConfirmRecording = { anyActionCalled = true },
                onCancelRecording = { anyActionCalled = true },
                onLockRecording = { anyActionCalled = true },
            )

            handleDragResult(DragResult.AlreadyLocked, { RecordingState.Hold() }, actions)

            assertFalse(anyActionCalled)
        }

        @Test
        fun `Released confirms when state is Hold with offset`() {
            var confirmed = false
            val actions = AudioRecordingActions.None.copy(
                onConfirmRecording = { confirmed = true },
            )
            val holdState = RecordingState.Hold(
                durationInMs = 500,
                offset = Pair(-20f, -10f),
            )

            handleDragResult(DragResult.Released, { holdState }, actions)

            assertTrue(confirmed)
        }

        @Test
        fun `Released confirms when state is Idle`() {
            var confirmed = false
            val actions = AudioRecordingActions.None.copy(
                onConfirmRecording = { confirmed = true },
            )

            handleDragResult(DragResult.Released, { RecordingState.Idle }, actions)

            assertTrue(confirmed)
        }
    }
}
