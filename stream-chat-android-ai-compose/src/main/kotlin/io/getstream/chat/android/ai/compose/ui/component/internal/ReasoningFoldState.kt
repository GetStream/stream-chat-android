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

import android.os.SystemClock
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlin.math.max

/**
 * Whether a reasoning view is open, and what the reader chose.
 *
 * Until the reader taps the header, the reasoning is open while the model thinks (if
 * `showsLiveReasoning`) and folded once it is done (unless `initiallyExpanded`). Live reasoning
 * joins the layout at no height and unfolds once it appears, rather than pushing everything below
 * it aside in one frame.
 */
@Stable
internal class ReasoningFoldState(
    isThinking: Boolean,
    showsLiveReasoning: Boolean,
    initiallyExpanded: Boolean,
    startsOpen: Boolean,
) {
    /** What the reader chose by tapping the header, or `null` until they do. */
    var choice: Boolean? by mutableStateOf(null)
        private set

    /** Whether the reasoning is open, or opening. */
    var open: Boolean by mutableStateOf(reasoningOpens(isThinking, showsLiveReasoning, initiallyExpanded))
        private set

    /** Drives the panel's fold. */
    val visibility: MutableTransitionState<Boolean> = MutableTransitionState(open && startsOpen)

    /** How long the model had thought when this view saw it stop. */
    var thoughtFor: Double? by mutableStateOf(null)
        private set

    private var thinkingSince = SystemClock.elapsedRealtime()
    private var lastThinking = isThinking

    /** The reader tapped the header. */
    fun toggle() {
        choice = !open
        open = !open
    }

    /** Whether the footnote shows: only once the reader opened finished reasoning, or it opens by default. */
    fun showsFootnote(isThinking: Boolean, initiallyExpanded: Boolean): Boolean =
        !isThinking && (choice == true || initiallyExpanded)

    /** Follows the model starting or stopping to think, then folds or opens the reasoning. */
    suspend fun thinkingChanged(
        isThinking: Boolean,
        durationSeconds: Double?,
        showsLiveReasoning: Boolean,
        initiallyExpanded: Boolean,
    ) {
        if (lastThinking == isThinking) return
        lastThinking = isThinking
        val now = SystemClock.elapsedRealtime()
        if (isThinking) {
            thinkingSince = now
        } else {
            thoughtFor = max(durationSeconds ?: 0.0, (now - thinkingSince) / MillisPerSecond)
        }
        if (choice ?: reasoningOpens(isThinking, showsLiveReasoning, initiallyExpanded)) {
            open = true
            return
        }
        // Folded in its own change, after the update that ended the thinking, so the whole layout
        // glides with it.
        withFrameNanos { }
        if (choice == null) open = false
    }
}

/**
 * Whether the reasoning is open when the reader has not chosen: while the model thinks if
 * [showsLiveReasoning], and once it is done if [initiallyExpanded].
 */
internal fun reasoningOpens(isThinking: Boolean, showsLiveReasoning: Boolean, initiallyExpanded: Boolean): Boolean =
    if (isThinking) showsLiveReasoning else initiallyExpanded

private const val MillisPerSecond = 1000.0
