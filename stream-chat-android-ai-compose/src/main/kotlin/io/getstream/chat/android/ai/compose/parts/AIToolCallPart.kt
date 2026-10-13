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
 * A tool the agent called, run by the agent's backend or by a person's device.
 *
 * @param id The model provider's tool-call ID. A device's result is matched against it.
 * @param name The tool's name, as the agent declared it.
 * @param displayTitle What the call is doing, in words for people, such as "Checking your
 * location".
 * @param status Where the call is. Defaults to [Status.Running] when the agent sends none.
 * @param executor Who runs the tool. Defaults to [Executor.Server] when the agent sends none.
 * @param targetUserId The person whose device must run a client tool.
 * @param targetClientId The install that must run a client tool, from the custom data of the
 * person's triggering message.
 * @param arguments The call's arguments as JSON text with sorted keys, present for client tools,
 * which need them to run. Every channel member can see them.
 * @param summary A short, shareable outcome, such as "Found your location".
 * @param durationMs How long the call took, in milliseconds.
 * @param approval What the call asks the person it waits for before it runs, and how they
 * answered, when its tool asks first.
 */
@Suppress("LongParameterList", "DataClassContainsFunctions") // One property per field of the step.
public data class AIToolCallPart(
    val id: String,
    val name: String,
    val displayTitle: String? = null,
    val status: Status,
    val executor: Executor = Executor.Server,
    val targetUserId: String? = null,
    val targetClientId: String? = null,
    val arguments: String? = null,
    val summary: String? = null,
    val durationMs: Int? = null,
    val approval: AIToolApproval? = null,
) {

    /** Whether the person declined the call, so it never ran. */
    public val isDeclined: Boolean get() = approval?.decision == AIToolApproval.Decision.Declined

    /** How long the call took, in seconds. */
    public val durationSeconds: Double? get() = durationMs?.let { it / MillisPerSecond }

    /**
     * Whether this call is waiting for this device: a client tool, still awaiting its result,
     * targeted at this person and this install.
     *
     * @param userId The person signed in on this device.
     * @param clientId This install, from [AIClientIdentity.installId].
     */
    public fun isAwaiting(userId: String, clientId: String): Boolean =
        executor == Executor.Client && status == Status.AwaitingClient &&
            targetUserId == userId && targetClientId == clientId

    /**
     * Whether this call is waiting for this person to allow it, on this device: still awaiting
     * approval, targeted at this person, and at this install when it names one (a client tool
     * does; a server tool's question may be answered from any of their devices).
     *
     * @param userId The person signed in on this device.
     * @param clientId This install, from [AIClientIdentity.installId].
     */
    public fun isAwaitingApproval(userId: String, clientId: String): Boolean =
        status == Status.AwaitingApproval && approval != null && targetUserId == userId &&
            (targetClientId == null || targetClientId == clientId)

    /**
     * Where a tool call is. An open set: compare against the statuses you know, and treat the rest
     * as still in progress.
     *
     * @param rawValue The status as the agent wrote it.
     */
    @JvmInline
    public value class Status(public val rawValue: String) {

        /** Whether the call has finished, one way or another. */
        public val isFinished: Boolean get() = this == Completed || this == Failed || this == Cancelled

        override fun toString(): String = rawValue

        public companion object {
            /** The agent's backend is running the call. */
            public val Running: Status = Status("running")

            /**
             * Waiting for the targeted person to allow or decline the call. Its [approval] says
             * what to ask them.
             */
            public val AwaitingApproval: Status = Status("awaiting_approval")

            /** Waiting for the targeted device to run the tool and send its result. */
            public val AwaitingClient: Status = Status("awaiting_client")

            /** The call finished and its result went back to the model. */
            public val Completed: Status = Status("completed")

            /** The call failed. */
            public val Failed: Status = Status("failed")

            /** The call was cancelled. */
            public val Cancelled: Status = Status("cancelled")
        }
    }

    /**
     * Who runs a tool. An open set: compare against the executors you know.
     *
     * @param rawValue The executor as the agent wrote it.
     */
    @JvmInline
    public value class Executor(public val rawValue: String) {
        override fun toString(): String = rawValue

        public companion object {
            /** The agent's backend. */
            public val Server: Executor = Executor("server")

            /** A person's device. */
            public val Client: Executor = Executor("client")
        }
    }

    internal companion object {
        fun from(id: String, fields: Fields): AIToolCallPart = AIToolCallPart(
            id = id,
            name = fields.string("name").orEmpty(),
            displayTitle = fields.string("display_title"),
            status = fields.string("status")?.let(::Status) ?: Status.Running,
            executor = fields.string("executor")?.let(::Executor) ?: Executor.Server,
            targetUserId = fields.string("target_user_id"),
            targetClientId = fields.string("target_client_id"),
            arguments = fields.json("arguments"),
            summary = fields.string("summary"),
            durationMs = fields.int("duration_ms"),
            approval = fields.nested("approval")?.let(AIToolApproval::from),
        )
    }
}
