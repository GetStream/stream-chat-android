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

package io.getstream.chat.android.ai.compose.ui.component.internal

import androidx.compose.runtime.MonotonicFrameClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

@RunWith(AndroidJUnit4::class)
internal class ReasoningFoldStateTest {

    private fun state(
        isThinking: Boolean,
        showsLiveReasoning: Boolean = true,
        initiallyExpanded: Boolean = false,
        startsOpen: Boolean = true,
    ) = ReasoningFoldState(isThinking, showsLiveReasoning, initiallyExpanded, startsOpen)

    private suspend fun ReasoningFoldState.changed(
        isThinking: Boolean,
        durationSeconds: Double? = null,
        showsLiveReasoning: Boolean = true,
        initiallyExpanded: Boolean = false,
    ) = withContext(ImmediateFrameClock) {
        thinkingChanged(isThinking, durationSeconds, showsLiveReasoning, initiallyExpanded)
    }

    @Test
    fun `live reasoning is open while the model thinks`() {
        val state = state(isThinking = true)

        assertTrue(state.open)
        assertNull(state.choice)
    }

    @Test
    fun `live reasoning that starts at no height joins the layout folded`() {
        val state = state(isThinking = true, startsOpen = false)

        assertTrue(state.open)
        assertFalse(state.visibility.currentState)
    }

    @Test
    fun `finished reasoning starts folded unless it opens by default`() {
        assertFalse(state(isThinking = false).open)
        assertTrue(state(isThinking = false, initiallyExpanded = true).open)
    }

    @Test
    fun `tapping the header flips the reasoning and remembers the choice`() {
        val state = state(isThinking = false)

        state.toggle()
        assertTrue(state.open)
        assertEquals(true, state.choice)

        state.toggle()
        assertFalse(state.open)
        assertEquals(false, state.choice)
    }

    @Test
    fun `the footnote shows only on finished reasoning the reader opened or that opens by default`() {
        val state = state(isThinking = false)
        assertFalse(state.showsFootnote(isThinking = false, initiallyExpanded = false))
        assertTrue(state.showsFootnote(isThinking = false, initiallyExpanded = true))

        state.toggle()
        assertTrue(state.showsFootnote(isThinking = false, initiallyExpanded = false))
        assertFalse(state.showsFootnote(isThinking = true, initiallyExpanded = false))
    }

    @Test
    fun `reasoning folds once the model stops thinking`() = runTest {
        val state = state(isThinking = true)

        state.changed(isThinking = false)

        assertFalse(state.open)
    }

    @Test
    fun `reasoning that opens by default stays open when the model stops`() = runTest {
        val state = state(isThinking = true)

        state.changed(isThinking = false, initiallyExpanded = true)

        assertTrue(state.open)
    }

    @Test
    fun `reasoning the reader opened stays open when the model stops`() = runTest {
        val state = state(isThinking = true, showsLiveReasoning = false)
        state.toggle()

        state.changed(isThinking = false, showsLiveReasoning = false)

        assertTrue(state.open)
    }

    @Test
    fun `reasoning the reader closed stays closed when the model stops`() = runTest {
        val state = state(isThinking = true)
        state.toggle()

        state.changed(isThinking = false)

        assertFalse(state.open)
    }

    @Test
    fun `the same thinking state again changes nothing`() = runTest {
        val state = state(isThinking = true)

        state.changed(isThinking = true)

        assertTrue(state.open)
        assertNull(state.thoughtFor)
    }

    @Test
    fun `thought-for is the time this view saw the model think`() = runTest {
        val state = state(isThinking = false)
        state.changed(isThinking = true)
        ShadowSystemClock.advanceBy(Duration.ofMillis(ThreeSeconds))

        state.changed(isThinking = false)

        assertEquals(3.0, state.thoughtFor!!, 0.01)
    }

    @Test
    fun `thought-for prefers the agent's longer duration`() = runTest {
        val state = state(isThinking = false)
        state.changed(isThinking = true)
        ShadowSystemClock.advanceBy(Duration.ofMillis(ThreeSeconds))

        state.changed(isThinking = false, durationSeconds = 12.0)

        assertEquals(12.0, state.thoughtFor!!, 0.01)
    }

    @Test
    fun `reasoning opens again when the model thinks again`() = runTest {
        val state = state(isThinking = true)
        state.changed(isThinking = false)

        state.changed(isThinking = true)

        assertTrue(state.open)
    }

    private object ImmediateFrameClock : MonotonicFrameClock {
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R = onFrame(0L)
    }

    private companion object {
        const val ThreeSeconds = 3_000L
    }
}
