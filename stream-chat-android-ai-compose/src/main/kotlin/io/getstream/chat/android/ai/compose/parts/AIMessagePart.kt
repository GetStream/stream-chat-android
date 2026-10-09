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
 * One step an AI agent took while replying, such as a round of reasoning or a tool call.
 *
 * The agent writes each step as a custom attachment on its reply (`ai_reasoning`, `ai_tool_call`),
 * and the order of the attachments is the order of the steps. The final answer stays in the
 * message text and is shown after them.
 *
 * The kinds of step are open: a new one can appear without breaking your code. Read the ones you
 * know through their typed views ([reasoning], [toolCall]), read kinds of your own from [payload],
 * and give anything else a neutral fallback (`AIMessagePartItem` shows a placeholder). Decoding is
 * lenient: missing fields get defaults, and a step newer than this SDK understands keeps its
 * payload but has no typed view.
 *
 * ```
 * // Stream Chat Android moves an attachment's `name` out of `extraData`, so put it back.
 * val parts = AIMessagePart.parts(
 *     message.attachments.map { it.type.orEmpty() to it.extraData + ("name" to it.name) },
 * )
 * for (part in parts) {
 *     part.reasoning?.let { /* … */ }
 *     part.toolCall?.let { /* … */ }
 *     if (part.kind == AIMessagePart.Kind("ai_citation")) { /* read part.payload */ }
 * }
 * ```
 */
public class AIMessagePart private constructor(
    /** What kind of step this is. */
    public val kind: Kind,
    /**
     * The step's stable identity, for diffing and animating while it streams. Tool calls use the
     * model provider's tool-call ID.
     */
    public val id: String,
    /** The step's format version (`v`). It changes only when a kind changes incompatibly. */
    public val version: Int,
    /** The step's attachment payload, for kinds of your own or fields this SDK does not read. */
    public val payload: Map<String, Any?>,
    /** The step as a round of reasoning, when it is one this SDK can read. */
    public val reasoning: AIReasoningPart?,
    /** The step as a tool call, when it is one this SDK can read. */
    public val toolCall: AIToolCallPart?,
) {

    /**
     * Whether this SDK has a typed view of the step. A step that is not, from a newer agent or of
     * a kind of your own, still keeps its payload.
     */
    public val isSupported: Boolean get() = reasoning != null || toolCall != null

    override fun equals(other: Any?): Boolean = other is AIMessagePart &&
        kind == other.kind && id == other.id && version == other.version && payload == other.payload

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + version
        result = 31 * result + payload.hashCode()
        return result
    }

    override fun toString(): String = "AIMessagePart(kind=${kind.rawValue}, id=$id, version=$version)"

    /**
     * What kind of step a part is. It is an open set: compare against the kinds you know and give
     * the rest a fallback.
     *
     * @param rawValue The attachment type, such as `ai_reasoning`.
     */
    @JvmInline
    public value class Kind(public val rawValue: String) {
        override fun toString(): String = rawValue

        public companion object {
            /** A round of the model's reasoning (`ai_reasoning`). */
            public val Reasoning: Kind = Kind("ai_reasoning")

            /** A tool the agent called (`ai_tool_call`). */
            public val ToolCall: Kind = Kind("ai_tool_call")
        }
    }

    public companion object {
        /** The attachment type prefix every AI step uses. */
        public const val TYPE_PREFIX: String = "ai_"

        /** The newest format version of the built-in kinds this SDK understands. */
        public const val SUPPORTED_VERSION: Int = 1

        /**
         * Decodes the AI steps among a message's attachments, in order. Attachments that are not
         * AI steps (images, files and so on) are skipped.
         *
         * @param attachments Each attachment's type and payload, in the message's order.
         */
        public fun parts(attachments: List<Pair<String, Map<String, Any?>>>): List<AIMessagePart> =
            attachments.mapIndexedNotNull { index, (type, payload) -> from(type, payload, index) }

        /**
         * Decodes the AI steps among a message's attachments, with each payload as JSON text, in
         * order. Attachments that are not AI steps are skipped, and a payload that is not a JSON
         * object reads as empty.
         *
         * @param attachments Each attachment's type and payload as JSON text, in the message's
         * order.
         */
        public fun partsFromJson(attachments: List<Pair<String, String>>): List<AIMessagePart> =
            attachments.mapIndexedNotNull { index, (type, json) -> fromJson(type, json, index) }

        /**
         * Decodes one attachment, or returns `null` when it is not an AI step.
         *
         * @param type The attachment type.
         * @param payload The attachment payload.
         * @param position The attachment's index, used as the identity of a step that carries no
         * ID of its own.
         */
        public fun from(type: String, payload: Map<String, Any?>, position: Int = 0): AIMessagePart? {
            if (!type.startsWith(TYPE_PREFIX)) return null
            val fields = Fields(payload)
            val id = fields.string("id") ?: "$type-$position"
            val version = fields.int("v") ?: 1
            val kind = Kind(type)
            // A newer format of a known kind keeps its payload but has no typed view.
            val known = version <= SUPPORTED_VERSION
            return AIMessagePart(
                kind = kind,
                id = id,
                version = version,
                payload = payload,
                reasoning = AIReasoningPart.from(id, fields).takeIf { known && kind == Kind.Reasoning },
                toolCall = AIToolCallPart.from(id, fields).takeIf { known && kind == Kind.ToolCall },
            )
        }

        /**
         * Decodes one attachment from its payload as JSON text, or returns `null` when it is not
         * an AI step. A payload that is not a JSON object reads as empty.
         *
         * @param type The attachment type.
         * @param json The attachment payload as JSON text.
         * @param position The attachment's index, used as the identity of a step that carries no
         * ID of its own.
         */
        public fun fromJson(type: String, json: String, position: Int = 0): AIMessagePart? {
            if (!type.startsWith(TYPE_PREFIX)) return null
            return from(type, Json.parseObject(json), position)
        }
    }
}
