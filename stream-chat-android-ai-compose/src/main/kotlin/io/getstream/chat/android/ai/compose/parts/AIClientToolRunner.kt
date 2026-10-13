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

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

/**
 * A tool this device runs when an AI agent asks for it.
 *
 * The agent writes the call on its reply as an `ai_tool_call` step with `executor: client` and
 * `status: awaiting_client`, addressed to one person and one install. An [AIClientToolRunner] on
 * that install runs it and sends the result back.
 */
public interface AIClientTool {

    /** The tool's name, as the agent declared it. */
    public val name: String

    /**
     * Runs one call.
     *
     * @param call The call to run, with its arguments.
     * @return What the device reports for the call. If this throws or times out, the runner reports
     * the call as failed.
     */
    public suspend fun run(call: AIToolCallPart): AIClientToolResult
}

/**
 * What a device reports for a call.
 *
 * @param output The result for the model, as JSON object text.
 * @param summary A short outcome every channel member sees on the step, such as "Shared
 * approximate location". Keep the data itself out of it.
 * @param failure Why the device could not run the call, such as "Location not shared". Shown on
 * the step and told to the model.
 */
public data class AIClientToolResult internal constructor(
    val output: String? = null,
    val summary: String? = null,
    val failure: String? = null,
) {
    public companion object {
        /**
         * A completed call, with its result for the model.
         *
         * @param outputJson The result for the model, as JSON object text.
         * @param summary A short, shareable outcome.
         */
        public fun completed(outputJson: String, summary: String? = null): AIClientToolResult =
            AIClientToolResult(output = outputJson, summary = summary)

        /**
         * A call the device could not run.
         *
         * @param reason Why the device could not run the call.
         */
        public fun failed(reason: String): AIClientToolResult = AIClientToolResult(failure = reason)
    }
}

/**
 * Runs the client tool calls addressed to this device, each once.
 *
 * Give it each AI reply's steps as they update. It runs a call when the call has its own id,
 * awaits this person and this install, the runner has a tool of that name and the call has not run
 * here before, then hands the result to `send`. A result that could not be sent is sent again on a later update,
 * without running the tool again.
 *
 * The runner is confined to the main thread: call [run] from it, and pass a [scope] that runs on
 * it, such as a `viewModelScope` or `rememberCoroutineScope()`.
 *
 * ```
 * val runner = AIClientToolRunner(userId, AIClientIdentity.installId(context), listOf(LocationTool()), scope)
 * runner.run(parts) { call, result -> backend.send(result, call, message) }
 * ```
 *
 * @param userId The person signed in on this device.
 * @param clientId This install, from [AIClientIdentity.installId].
 * @param tools The tools this device can run. When two share a name, the first is used.
 * @param scope Where tools run and results are sent.
 */
public class AIClientToolRunner(
    public val userId: String,
    public val clientId: String,
    tools: List<AIClientTool>,
    private val scope: CoroutineScope,
) {
    private val tools: Map<String, AIClientTool> = tools.distinctBy { it.name }.associateBy { it.name }
    private val calls = mutableMapOf<String, Call>()

    /** How many times a result is offered to `send` before the runner gives up on it. At least 1. */
    public var maxAttempts: Int = DEFAULT_MAX_ATTEMPTS
        set(value) {
            require(value >= 1) { "maxAttempts must be at least 1, was $value" }
            field = value
        }

    /** The names of the tools this device can run. */
    public val toolNames: List<String> get() = tools.keys.sorted()

    private class Call {
        var result: AIClientToolResult? = null
        var attempts = 0
        var sending = false
        var sent = false
    }

    /**
     * Runs the calls among [parts] that await this device, and sends their results.
     *
     * @param parts A reply's steps.
     * @param send Sends a call's result to your backend. Throw to have it offered again on a later
     * update that still shows the call waiting.
     */
    public fun run(
        parts: List<AIMessagePart>,
        send: suspend (call: AIToolCallPart, result: AIClientToolResult) -> Unit,
    ) {
        // A call without its own id gets a positional one, which repeats across replies and
        // could not match its result.
        parts.filter { Fields(it.payload).string("id") != null }
            .mapNotNull { it.toolCall }
            .filter { it.isAwaiting(userId, clientId) }
            .forEach { call -> start(call, send) }
    }

    private fun start(call: AIToolCallPart, send: suspend (AIToolCallPart, AIClientToolResult) -> Unit) {
        val tool = tools[call.name] ?: return
        val state = calls.getOrPut(call.id, ::Call)
        if (state.sending || state.sent || state.attempts >= maxAttempts) return
        state.sending = true
        val done = state.result
        scope.launch {
            try {
                val result = done ?: runTool(tool, call)
                deliver(state, result, call, send)
            } finally {
                state.sending = false
            }
        }
    }

    // A tool that throws or times out is reported as failed, so the agent gets an answer.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun runTool(tool: AIClientTool, call: AIToolCallPart): AIClientToolResult =
        try {
            tool.run(call)
        } catch (cancellation: CancellationException) {
            // Rethrows when the runner's scope is cancelled; otherwise the tool's own timeout.
            currentCoroutineContext().ensureActive()
            AIClientToolResult.failed(TOOL_FAILED)
        } catch (_: Exception) {
            AIClientToolResult.failed(TOOL_FAILED)
        }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun deliver(
        state: Call,
        result: AIClientToolResult,
        call: AIToolCallPart,
        send: suspend (AIToolCallPart, AIClientToolResult) -> Unit,
    ) {
        state.result = result
        state.attempts += 1
        try {
            send(call, result)
            state.sent = true
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Offered again on the next update that still shows the call waiting.
        }
    }

    private companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val TOOL_FAILED = "The tool failed on this device."
    }
}

/**
 * A stable identifier for this install, for addressing client tool calls to it. Put it in the
 * custom data of the person's message (`client_id`) so the agent can copy it onto the calls it
 * makes while answering.
 */
public object AIClientIdentity {
    private const val FILE = "io.getstream.ai.client-id"

    /**
     * This install's identifier, created on first use and kept until the app's data is cleared.
     * It is not backed up, so a device restored from a backup gets its own. Reads and writes a
     * small file, so prefer calling it off the main thread.
     *
     * @param context Any context; the application context is used.
     */
    @Synchronized
    public fun installId(context: Context): String {
        // noBackupFilesDir: a restored device must not answer calls meant for the old one.
        val file = File(context.applicationContext.noBackupFilesDir, FILE)
        if (file.exists()) file.readText().takeIf { it.isNotBlank() }?.let { return it }
        val created = "android-" + UUID.randomUUID().toString().uppercase(Locale.ROOT)
        file.writeText(created)
        return created
    }
}
