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

package io.getstream.chat.android.ai.compose.parts

/**
 * A round of the model's reasoning.
 *
 * @param id The step's stable identity.
 * @param status Where the round is. Defaults to [Status.Completed] when the agent sends none.
 * @param summary A one-line summary of the reasoning, once it is done.
 * @param preview A capped excerpt: the latest thoughts while streaming, the opening once done. The
 * full reasoning, when an app has it, arrives separately.
 * @param durationMs How long the model thought, in milliseconds.
 */
public data class AIReasoningPart(
    val id: String,
    val status: Status,
    val summary: String? = null,
    val preview: String? = null,
    val durationMs: Int? = null,
) {

    /** Whether the model is still thinking. */
    public val isStreaming: Boolean get() = status == Status.Streaming

    /** How long the model thought, in seconds. */
    public val durationSeconds: Double? get() = durationMs?.let { it / MillisPerSecond }

    /**
     * Where a round of reasoning is. An open set: compare against the statuses you know.
     *
     * @param rawValue The status as the agent wrote it.
     */
    @JvmInline
    public value class Status(public val rawValue: String) {
        override fun toString(): String = rawValue

        public companion object {
            /** The model is still thinking. */
            public val Streaming: Status = Status("streaming")

            /** The round is over. */
            public val Completed: Status = Status("completed")
        }
    }

    internal companion object {
        fun from(id: String, fields: Fields): AIReasoningPart = AIReasoningPart(
            id = id,
            status = fields.string("status")?.let(::Status) ?: Status.Completed,
            summary = fields.string("summary"),
            preview = fields.string("preview"),
            durationMs = fields.int("duration_ms"),
        )
    }
}

internal const val MillisPerSecond = 1000.0
