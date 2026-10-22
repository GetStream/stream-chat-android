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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import io.getstream.chat.android.ai.compose.ui.component.StreamingReasoningDefaults
import kotlin.math.max

/**
 * Times the thinking of each turn a conversation sees start, so the thinking shown before anything
 * of a reply arrives and the reasoning step that takes its place read as one: "Thinking… 8s", then
 * "Thought for 8s". A model that sends its reasoning only with its answer sends a step with no time
 * of its own.
 *
 * Turns are told apart by equality, so pass the same key for a turn on every update, such as the
 * id of the person's message that starts it.
 *
 * @param now The current time in milliseconds, on the clock of [SystemClock.elapsedRealtime].
 */
internal class AIThinkingClock(val now: () -> Long = SystemClock::elapsedRealtime) {

    private class Turn(val start: Long) {
        var end: Long? = null
        var firstStep: String? = null
        var firstStepStreamed = false
        var firstStepDuration: Double? = null

        // When a first step that arrived done opened to show its reasoning.
        var firstStepShown: Long? = null

        // Whether a reply of the turn showed.
        var hadReplies = false
    }

    private val turns = mutableMapOf<Any, Turn>()

    /**
     * Notes the latest turn and whether the agent is thinking about it with nothing shown yet.
     *
     * A turn's thinking starts the first time it is the latest turn and thinking, and ends when it
     * stops. Thinking that resumes before the first step shows carries on, rather than ending at
     * the pause. A turn whose replies were taken away, such as to ask for a new one, starts
     * thinking afresh. It repeats harmlessly on every update.
     */
    fun update(latestTurn: Any?, isThinking: Boolean, hasReplies: Boolean = false) {
        if (latestTurn == null) return
        val timed = turns[latestTurn]
        val repliesTakenAway = timed?.hadReplies == true && !hasReplies
        if (timed == null || (isThinking && repliesTakenAway)) {
            if (isThinking) turns[latestTurn] = Turn(start = now())
            return
        }
        if (hasReplies) timed.hadReplies = true
        if (isThinking) {
            if (timed.firstStep == null) timed.end = null
        } else if (timed.end == null) {
            timed.end = now()
        }
    }

    /** When the agent started thinking about [turn], on the clock of [now], or `null`. */
    fun startedAt(turn: Any): Long? = turns[turn]?.start

    /**
     * Whether [step] takes over from a turn's thinking: the first reasoning step to show in a turn
     * that was seen thinking. It stays first when its view is made again.
     */
    fun isFirstStep(step: String, turn: Any): Boolean {
        val timed = turns[turn] ?: return false
        if (timed.firstStep == null) timed.firstStep = step
        return timed.firstStep == step
    }

    /** Notes that a turn's first step was seen while the model was still thinking. */
    fun firstStepIsStreaming(turn: Any) {
        turns[turn]?.firstStepStreamed = true
    }

    /**
     * How long a turn's first step thought, in seconds: from when the turn started thinking until
     * the step was done. That is now for a step seen streaming, and when the turn stopped thinking
     * for one that arrived done. The step's own time, [ownSeconds], counts when it is longer. It is
     * measured once, so the step reads the same when its view is made again.
     */
    fun durationOfFirstStep(turn: Any, ownSeconds: Double?): Double? {
        val timed = turns[turn] ?: return ownSeconds
        timed.firstStepDuration?.let { return it }
        val done = if (timed.firstStepStreamed) now() else timed.end ?: now()
        val measured = max(ownSeconds ?: 0.0, (done - timed.start) / MILLIS_PER_SECOND)
        timed.firstStepDuration = measured
        return measured
    }

    /**
     * Whether a turn's first step, which arrived done with the reply that ended the thinking, opens
     * to show its reasoning before it folds and the answer starts. It does as it arrives: a step
     * that thought in view showed its reasoning as it came, and one made again later stays folded.
     */
    fun showsFirstStep(turn: Any): Boolean {
        val timed = turns[turn]
        if (timed == null || timed.firstStepStreamed) return false
        val now = now()
        timed.firstStepShown?.let { return now - it < ARRIVAL_WINDOW_MILLIS }
        val end = timed.end
        if (end == null || now - end >= ARRIVAL_WINDOW_MILLIS) return false
        timed.firstStepShown = now
        return true
    }

    /**
     * When a turn's answer starts, on the clock of [now]: once a first step that showed its
     * reasoning has folded. `null` when there is nothing to wait for.
     */
    fun answerStartsAt(turn: Any): Long? = turns[turn]?.firstStepShown?.let {
        it + FINISHED_REASONING_SHOWN_MILLIS + StreamingReasoningDefaults.FoldDurationMillis
    }

    companion object {
        /**
         * Saves the turns' times, so a conversation shown again, such as after a rotation, counts
         * from when each turn started thinking. The turns' keys must be saveable in a Bundle.
         */
        val Saver: Saver<AIThinkingClock, Any> = listSaver(
            save = { clock ->
                clock.turns.flatMap { (key, turn) ->
                    with(turn) {
                        listOf(key, start, end, firstStep, firstStepStreamed) +
                            listOf(firstStepDuration, firstStepShown, hadReplies)
                    }
                }
            },
            restore = { saved ->
                AIThinkingClock().apply {
                    saved.chunked(SAVED_FIELDS).forEach { fields ->
                        turns[fields[0]!!] = Turn(start = fields[1] as Long).apply {
                            end = fields[2] as Long?
                            firstStep = fields[3] as String?
                            firstStepStreamed = fields[4] as Boolean
                            firstStepDuration = fields[5] as Double?
                            firstStepShown = fields[6] as Long?
                            hadReplies = fields[7] as Boolean
                        }
                    }
                }
            },
        )

        /**
         * How soon after a turn stops thinking its first step has to arrive, done, to open and show
         * its reasoning: it came with the reply that ended the thinking.
         */
        const val ARRIVAL_WINDOW_MILLIS: Long = 1000

        /**
         * How long a first step that arrived done stays open, showing its reasoning, before it
         * folds and the answer starts.
         */
        const val FINISHED_REASONING_SHOWN_MILLIS: Long = 1600

        private const val MILLIS_PER_SECOND = 1000.0
        private const val SAVED_FIELDS = 8
    }
}
