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

package io.getstream.chat.android.ai.ondevice

import android.content.Context
import android.content.pm.PackageManager
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.SystemInstruction
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

/**
 * Gemini Nano, the model in Android's AICore, through ML Kit's GenAI Prompt API. It answers
 * without a network connection, and the conversation never leaves the device.
 *
 * It runs on devices whose AICore offers Gemini Nano (recent Pixel, Samsung and other flagship
 * phones), not on emulators. The model may need downloading first: [status] says whether it is
 * [Status.Downloadable] or [Status.Downloading], and [download] starts it.
 *
 * It is a small model, good for short answers and for working with what is in the conversation.
 * Its context holds a few thousand tokens, so the oldest turns of a long conversation are left out.
 *
 * ```
 * val model = AIOnDeviceModel(context)
 * if (model.isAvailable()) {
 *     model.reply(instructions, turns).collect { answer -> show(answer) }
 * }
 * ```
 *
 * @param context Any context; the application context is kept.
 * @param maximumResponseTokens The longest answer, in tokens. The model writes at most
 * [MODEL_MAXIMUM_RESPONSE_TOKENS], so larger values are capped.
 */
public class AIOnDeviceModel(
    context: Context,
    public val maximumResponseTokens: Int = DEFAULT_RESPONSE_TOKENS,
) : AILocalModel, AutoCloseable {

    private val packageManager = context.applicationContext.packageManager
    private val model: GenerativeModel by lazy { Generation.getClient() }

    /** Where the model is on this device. */
    public enum class Status {
        /** The device can't run the model. */
        Unavailable,

        /** The device can run the model once it is downloaded; see [download]. */
        Downloadable,

        /** The model is downloading. */
        Downloading,

        /** The model can answer now. */
        Available,
    }

    /** How a download of the model is going. More states may be added. */
    public abstract class Download internal constructor() {
        /**
         * The download started.
         *
         * @param bytesToDownload The size of the download.
         */
        public data class Started(val bytesToDownload: Long) : Download()

        /**
         * Part of the model has arrived.
         *
         * @param bytesDownloaded How much has arrived so far.
         */
        public data class Progress(val bytesDownloaded: Long) : Download()

        /** The model is on the device and can answer. */
        public data object Completed : Download()

        /**
         * The download failed.
         *
         * @param cause Why.
         */
        public data class Failed(val cause: Throwable) : Download()
    }

    /** Thrown by [reply] when the model can't answer on this device. */
    public class Unavailable(cause: Throwable? = null) : Exception("The on-device model is unavailable.", cause)

    /**
     * Thrown by [reply] when the model fails while answering.
     *
     * @param cause The model's own error.
     */
    public class Failure(cause: Throwable) : Exception("The on-device model failed to answer.", cause)

    /** Where the model is on this device. A device without AICore, or an error, reads as unavailable. */
    @Suppress("TooGenericExceptionCaught")
    public suspend fun status(): Status {
        if (!hasAICore()) return Status.Unavailable
        return try {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> Status.Available
                FeatureStatus.DOWNLOADABLE -> Status.Downloadable
                FeatureStatus.DOWNLOADING -> Status.Downloading
                else -> Status.Unavailable
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            Status.Unavailable
        }
    }

    override suspend fun isAvailable(): Boolean = status() == Status.Available

    /**
     * Downloads the model, when [status] is [Status.Downloadable]. The download continues while
     * the flow is collected.
     */
    public fun download(): Flow<Download> = model.download().map { status ->
        when (status) {
            is DownloadStatus.DownloadStarted -> Download.Started(status.bytesToDownload)
            is DownloadStatus.DownloadProgress -> Download.Progress(status.totalBytesDownloaded)
            is DownloadStatus.DownloadCompleted -> Download.Completed
            is DownloadStatus.DownloadFailed -> Download.Failed(status.e)
        }
    }

    /**
     * Streams the answer to the last of [turns]. Each emission is the whole answer so far, and
     * cancelling the collection stops the model.
     *
     * @throws Unavailable When the model can't answer on this device, or the last turn is not the
     * person's question.
     * @throws Failure When the model fails while answering.
     */
    override fun reply(instructions: String, turns: List<AIConversationTurn>): Flow<String> = flow {
        if (status() != Status.Available) throw Unavailable()
        val answer = StringBuilder()
        model.generateContentStream(request(instructions, turns))
            .catch { error -> throw if (error is GenAiException) Failure(error) else error }
            .collect { chunk ->
                val piece = chunk.candidates.firstOrNull()?.text.orEmpty()
                if (piece.isNotEmpty()) {
                    answer.append(piece)
                    emit(answer.toString())
                }
            }
    }

    /**
     * The request for the newest turns that fit the model's context, with the instructions as its
     * system prompt when the model takes one.
     */
    private suspend fun request(instructions: String, turns: List<AIConversationTurn>): GenerateContentRequest {
        val responseTokens = maximumResponseTokens.coerceIn(1, MODEL_MAXIMUM_RESPONSE_TOKENS)
        val systemPrompt = supportsSystemPrompt()
        val budget = contextTokens() - responseTokens -
            OnDevicePrompt.estimatedTokens(instructions) - PROMPT_FRAMING_TOKENS
        val fitted = AIConversationTurn.fitting(turns, budget)
        val question = fitted.lastOrNull()?.takeIf { it.role == AIConversationTurn.Role.User } ?: throw Unavailable()
        val prompt = TextPart(
            OnDevicePrompt.text(
                instructions = instructions.takeUnless { systemPrompt },
                history = fitted.dropLast(1),
                question = question.text,
            ),
        )
        return if (systemPrompt) {
            generateContentRequest(SystemInstruction(instructions), prompt) { maxOutputTokens = responseTokens }
        } else {
            generateContentRequest(prompt) { maxOutputTokens = responseTokens }
        }
    }

    /** Releases the model's resources. */
    override fun close() {
        model.close()
    }

    private fun hasAICore(): Boolean = try {
        packageManager.getPackageInfo(AICORE_PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun contextTokens(): Int = try {
        model.getTokenLimit()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        DEFAULT_CONTEXT_TOKENS
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun supportsSystemPrompt(): Boolean = try {
        model.isSystemPromptAvailable()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        false
    }

    public companion object {
        /** The longest answer the model writes, in tokens. */
        public const val MODEL_MAXIMUM_RESPONSE_TOKENS: Int = 256

        private const val DEFAULT_RESPONSE_TOKENS = 1000
        private const val AICORE_PACKAGE = "com.google.android.aicore"

        /** Gemini Nano's context through ML Kit, when the model doesn't say. */
        private const val DEFAULT_CONTEXT_TOKENS = 4000

        /** Room for the labels that frame the conversation in the prompt. */
        private const val PROMPT_FRAMING_TOKENS = 50
    }
}

/** How a conversation is written as one prompt: the Prompt API takes text, not a transcript. */
internal object OnDevicePrompt {

    private const val BYTES_PER_TOKEN = 3

    fun text(instructions: String?, history: List<AIConversationTurn>, question: String): String = buildString {
        if (!instructions.isNullOrBlank()) {
            append(instructions.trim()).append("\n\n")
        }
        if (history.isNotEmpty()) {
            append("Conversation so far:\n")
            history.forEach { turn -> append(label(turn.role)).append(": ").append(turn.text.trim()).append('\n') }
            append('\n')
            append("Answer the user's last message.\n\n")
            append(label(AIConversationTurn.Role.User)).append(": ").append(question.trim())
        } else {
            append(question.trim())
        }
    }

    fun estimatedTokens(text: String): Int = text.toByteArray(Charsets.UTF_8).size / BYTES_PER_TOKEN

    private fun label(role: AIConversationTurn.Role): String = when (role) {
        AIConversationTurn.Role.User -> "User"
        AIConversationTurn.Role.Assistant -> "Assistant"
    }
}
