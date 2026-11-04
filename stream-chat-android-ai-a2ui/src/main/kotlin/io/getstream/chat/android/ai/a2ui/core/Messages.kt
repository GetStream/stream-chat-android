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

package io.getstream.chat.android.ai.a2ui.core

/**
 * One component of a surface. A2UI sends components as a flat list: each has an `id`, a
 * `component` type and its properties inline, and refers to its children by id.
 */
internal data class Component(
    val id: String,
    val type: String,
    val catalogId: String? = null,
    val accessibility: Map<String, Any?>? = null,
    val metadata: Map<String, Any?>? = null,
    val properties: Map<String, Any?> = emptyMap(),
    /** Whether the agent wrote a `catalogId` that isn't a string. */
    val hasInvalidCatalogId: Boolean = false,
) {
    /** The layout weight of the component in a `Row` or `Column`. */
    val weight: Double? get() = A2UIJson.double(properties["weight"])

    /** A property as the agent wrote it. */
    fun dynamic(key: String): DynamicValue = DynamicValue.of(properties[key])

    /** The checks the component declares. */
    val checks: List<CheckRule> get() = CheckRule.list(properties["checks"])

    /** The component as JSON. */
    fun toJson(): Map<String, Any?> = LinkedHashMap<String, Any?>().apply {
        put("id", id)
        put("component", type)
        catalogId?.let { put("catalogId", it) }
        accessibility?.let { put("accessibility", it) }
        metadata?.let { put("metadata", it) }
        putAll(properties)
    }

    companion object {
        private val ENVELOPE_KEYS = setOf("id", "component", "catalogId", "accessibility", "metadata")

        /** Reads a component, or `null` without an `id` and a `component` type. */
        fun of(json: Map<String, Any?>): Component? {
            val id = (json["id"] as? String)?.takeIf { it.isNotEmpty() } ?: return null
            val type = (json["component"] as? String)?.takeIf { it.isNotEmpty() } ?: return null
            return Component(
                id = id,
                type = type,
                catalogId = json["catalogId"] as? String,
                accessibility = A2UIJson.map(json["accessibility"])?.takeIf { it.isNotEmpty() },
                metadata = A2UIJson.map(json["metadata"]),
                properties = json.filterKeys { it !in ENVELOPE_KEYS },
                hasInvalidCatalogId = json.containsKey("catalogId") && json["catalogId"] !is String,
            )
        }
    }
}

/** A message from the agent to the renderer, as `agent_to_renderer.json` defines it. */
internal sealed interface AgentMessage {
    val surfaceId: String?

    data class CreateSurface(
        override val surfaceId: String,
        val catalogId: String?,
        val sendDataModel: Boolean,
        val components: List<Component>,
        val dataModel: Map<String, Any?>?,
        val metadata: Map<String, Any?>?,
        val invalidComponents: List<InvalidComponent> = emptyList(),
    ) : AgentMessage

    data class UpdateComponents(
        override val surfaceId: String,
        val components: List<Component>,
        val invalidComponents: List<InvalidComponent> = emptyList(),
    ) : AgentMessage

    /** Writes [value] at [path]; a `null` [value] removes the entry. */
    data class UpdateDataModel(override val surfaceId: String, val path: DataPath, val value: Any?) : AgentMessage

    data class DeleteSurface(override val surfaceId: String) : AgentMessage

    data class CallRendererFunction(
        val functionCallId: String,
        val call: FunctionCall,
        override val surfaceId: String?,
    ) : AgentMessage

    data class AgentFunctionResponse(val response: FunctionResponse) : AgentMessage {
        override val surfaceId: String? get() = null
    }
}

/**
 * An entry of a message's `components` that can't be read as a component.
 *
 * @param index The position of the entry in the message's `components`.
 * @param pointer The JSON Pointer, in the message, of what is wrong.
 */
internal data class InvalidComponent(val index: Int, val pointer: String, val reason: String) {
    companion object {
        /** Why the entry at [index], [json] when it is an object, isn't a component. */
        fun of(index: Int, json: Map<String, Any?>?): InvalidComponent {
            val pointer = "/components/$index"
            return when {
                json == null -> InvalidComponent(index, pointer, "Component entry $index must be an object.")
                (json["id"] as? String).isNullOrEmpty() ->
                    InvalidComponent(index, "$pointer/id", "Component entry $index has no 'id'.")
                else -> InvalidComponent(
                    index,
                    "$pointer/component",
                    "Component '${json["id"]}' has no 'component' type.",
                )
            }
        }

        /** The position in the message of each of [count] components read, skipping the [invalid] entries. */
        fun positions(count: Int, invalid: List<InvalidComponent>): List<Int> {
            val skipped = invalid.mapTo(HashSet()) { it.index }
            return generateSequence(0) { it + 1 }.filterNot(skipped::contains).take(count).toList()
        }
    }
}

/** The result of a function call, in either direction: a value or an error. */
internal data class FunctionResponse(
    val functionCallId: String,
    val value: Any? = null,
    val error: Failure? = null,
) {
    data class Failure(val code: String, val message: String)

    fun toJson(): Map<String, Any?> = buildMap {
        put("functionCallId", functionCallId)
        if (error != null) {
            put("error", mapOf("code" to error.code, "message" to error.message))
        } else {
            put("value", value)
        }
    }
}

/** A message that can't be read. */
internal sealed class DecodingException(message: String) : Exception(message) {
    class MissingField(val field: String, messageType: String) : DecodingException(
        if (field == "version") {
            "Missing required version field: every A2UI message carries \"version\": \"${A2UIProtocol.VERSION}\"."
        } else {
            "Missing required field '$field' in '$messageType' message."
        },
    )

    class InvalidField(val field: String, messageType: String, reason: String) :
        DecodingException("Invalid field '$field' in '$messageType' message. $reason")

    class UnsupportedVersion(version: String) :
        DecodingException("Unsupported protocol version '$version'. This renderer reads A2UI ${A2UIProtocol.VERSION}.")

    class UnknownMessageType(keys: Collection<String>) : DecodingException(
        keys.firstOrNull { it in LEGACY_MESSAGE_TYPES }
            ?.let { "The action '$it' is not supported in protocol version ${A2UIProtocol.VERSION}." }
            ?: "Unknown A2UI message type. Keys: ${keys.sorted().joinToString(", ")}",
    )

    class MultipleMessageTypes(keys: Collection<String>) : DecodingException(
        "Message contains multiple conflicting update actions: ${keys.sorted().joinToString(", ")}. " +
            "A message carries exactly one.",
    )

    /** A malformed `callRendererFunction`, which the renderer answers with `INVALID_FUNCTION_CALL`. */
    class InvalidFunctionCall(
        val functionCallId: String,
        reason: String,
    ) : DecodingException("Invalid function call. $reason")

    private companion object {
        // The messages of earlier versions, which a 1.0 agent might still write.
        val LEGACY_MESSAGE_TYPES = setOf("beginRendering", "surfaceUpdate", "dataModelUpdate")
    }

    /** The error to report to the agent, for the surface the message targeted. */
    fun rendererError(surfaceId: String?): RendererError = when (this) {
        is InvalidFunctionCall ->
            RendererError(RendererError.INVALID_FUNCTION_CALL, message.orEmpty(), functionCallId = functionCallId)
        is MissingField -> validationFailed(surfaceId, "/$field")
        is InvalidField -> validationFailed(surfaceId, "/$field")
        is UnsupportedVersion -> validationFailed(surfaceId, "/version")
        is UnknownMessageType, is MultipleMessageTypes -> validationFailed(surfaceId, "")
    }

    private fun validationFailed(surfaceId: String?, path: String) =
        RendererError(RendererError.VALIDATION_FAILED, message.orEmpty(), surfaceId.orEmpty(), path)
}

/** Reads agent-to-renderer messages. */
internal object AgentMessageDecoder {
    private val MESSAGE_TYPES = listOf(
        "createSurface",
        "updateComponents",
        "updateDataModel",
        "deleteSurface",
        "callRendererFunction",
        "agentFunctionResponse",
    )

    /** Whether [json] looks like an agent-to-renderer message. */
    fun isMessage(json: Map<String, Any?>): Boolean = MESSAGE_TYPES.any(json::containsKey)

    /** The surface the message in [json] targets, when it names one. */
    fun surfaceId(json: Map<String, Any?>): String? =
        MESSAGE_TYPES.firstNotNullOfOrNull { A2UIJson.map(json[it])?.get("surfaceId") as? String }

    /**
     * Reads one message. Its `version` must be 1.0, so an agent that writes another version
     * fails loudly rather than showing nothing.
     *
     * @throws DecodingException When it isn't a 1.0 message.
     */
    @Suppress("ThrowsCount")
    fun decode(json: Map<String, Any?>): AgentMessage {
        if (!json.containsKey("version")) throw DecodingException.MissingField("version", "envelope")
        val version = json["version"]
        if (version != A2UIProtocol.VERSION) throw DecodingException.UnsupportedVersion(A2UIJson.stringify(version))
        val keys = MESSAGE_TYPES.filter(json::containsKey)
        if (keys.size > 1) throw DecodingException.MultipleMessageTypes(keys)
        val key = keys.firstOrNull() ?: throw DecodingException.UnknownMessageType(json.keys)
        val payload = A2UIJson.map(json[key])
            ?: throw DecodingException.InvalidField(key, "envelope", "The message payload must be an object.")
        return when (key) {
            "createSurface" -> createSurface(payload)
            "updateComponents" -> updateComponents(payload)
            "updateDataModel" -> updateDataModel(payload)
            "deleteSurface" -> AgentMessage.DeleteSurface(surfaceId(payload, key))
            "callRendererFunction" -> callRendererFunction(payload)
            else -> AgentMessage.AgentFunctionResponse(functionResponse(payload, "functionResponse"))
        }
    }

    /** Reads a `rendererFunctionResponse` or `agentFunctionResponse` payload. */
    @Suppress("ThrowsCount")
    fun functionResponse(json: Map<String, Any?>, messageType: String): FunctionResponse {
        val callId = (json["functionCallId"] as? String)?.takeIf { it.isNotEmpty() }
            ?: throw DecodingException.MissingField("functionCallId", messageType)
        val hasValue = json.containsKey("value")
        val hasError = json.containsKey("error")
        if (hasValue == hasError) {
            throw DecodingException.InvalidField(
                "value",
                messageType,
                "Exactly one of 'value' and 'error' must be present.",
            )
        }
        if (hasError) {
            val error = A2UIJson.map(json["error"])
            val code = error?.get("code") as? String
            val message = error?.get("message") as? String
            if (code == null || message == null) {
                throw DecodingException.InvalidField(
                    "error",
                    messageType,
                    "The error must carry a 'code' and a 'message'.",
                )
            }
            return FunctionResponse(callId, error = FunctionResponse.Failure(code, message))
        }
        return FunctionResponse(callId, value = json["value"])
    }

    private fun surfaceId(payload: Map<String, Any?>, messageType: String): String {
        val raw = payload["surfaceId"]
        if (raw != null && raw !is String) {
            throw DecodingException.InvalidField("surfaceId", messageType, "surfaceId must be a string.")
        }
        return (raw as? String)?.takeIf { it.isNotEmpty() }
            ?: throw DecodingException.MissingField("surfaceId", messageType)
    }

    private fun createSurface(payload: Map<String, Any?>): AgentMessage.CreateSurface {
        val surfaceId = surfaceId(payload, "createSurface")
        payload["metadata"]?.let { raw ->
            val metadata = A2UIJson.map(raw)
                ?: throw DecodingException.InvalidField("metadata", "createSurface", "'metadata' must be an object.")
            ExtensionMetadata.violation(metadata)?.let { (pointer, reason) ->
                throw DecodingException.InvalidField("metadata$pointer", "createSurface", reason)
            }
        }
        val (components, invalidComponents) = components(payload["components"])
        return AgentMessage.CreateSurface(
            surfaceId = surfaceId,
            catalogId = payload["catalogId"] as? String,
            sendDataModel = A2UIJson.bool(payload["sendDataModel"]) ?: false,
            components = components,
            dataModel = A2UIJson.map(payload["dataModel"]),
            metadata = A2UIJson.map(payload["metadata"]),
            invalidComponents = invalidComponents,
        )
    }

    private fun updateComponents(payload: Map<String, Any?>): AgentMessage.UpdateComponents {
        val surfaceId = surfaceId(payload, "updateComponents")
        if (A2UIJson.list(payload["components"]) == null) {
            throw DecodingException.MissingField("components", "updateComponents")
        }
        val (components, invalidComponents) = components(payload["components"])
        return AgentMessage.UpdateComponents(surfaceId, components, invalidComponents)
    }

    @Suppress("ThrowsCount")
    private fun updateDataModel(payload: Map<String, Any?>): AgentMessage.UpdateDataModel {
        val surfaceId = surfaceId(payload, "updateDataModel")
        if (!payload.containsKey("value")) throw DecodingException.MissingField("value", "updateDataModel")
        var path = DataPath.Root
        if (payload.containsKey("path")) {
            val pointer = payload["path"] as? String
            if (pointer == null || pointer.isNotEmpty() && !pointer.startsWith("/")) {
                throw DecodingException.InvalidField("path", "updateDataModel", "It must be a JSON Pointer.")
            }
            if (!DataPath.isValidSyntax(pointer)) {
                throw DecodingException.InvalidField("path", "updateDataModel", "Invalid path syntax: '$pointer'.")
            }
            // An empty pointer is the whole document, as in JSON Pointer.
            path = if (pointer.isEmpty()) DataPath.Root else DataPath.parse(pointer)
        }
        return AgentMessage.UpdateDataModel(surfaceId, path, payload["value"])
    }

    @Suppress("ThrowsCount")
    private fun callRendererFunction(payload: Map<String, Any?>): AgentMessage.CallRendererFunction {
        val callId = (payload["functionCallId"] as? String)?.takeIf { it.isNotEmpty() }
            ?: throw DecodingException.MissingField("functionCallId", "callRendererFunction")
        val call = A2UIJson.map(payload["callFunction"])
            ?: throw DecodingException.InvalidFunctionCall(callId, "The 'callFunction' object is missing.")
        if (call["catalogId"] !is String) {
            throw DecodingException.InvalidFunctionCall(callId, "The function call must name its 'catalogId'.")
        }
        if (call.containsKey("args") && A2UIJson.map(call["args"]) == null) {
            throw DecodingException.InvalidFunctionCall(callId, "The function call 'args' must be an object.")
        }
        val function = FunctionCall.of(call)
            ?: throw DecodingException.InvalidFunctionCall(
                callId,
                "The function call must name the function in '@call'.",
            )
        return AgentMessage.CallRendererFunction(callId, function, payload["surfaceId"] as? String)
    }

    // The entries that read as components, and the ones that don't.
    private fun components(raw: Any?): Pair<List<Component>, List<InvalidComponent>> {
        val components = mutableListOf<Component>()
        val invalid = mutableListOf<InvalidComponent>()
        A2UIJson.list(raw).orEmpty().forEachIndexed { index, entry ->
            val json = A2UIJson.map(entry)
            val component = json?.let(Component::of)
            if (component != null) {
                components.add(component)
            } else {
                invalid.add(InvalidComponent.of(index, json))
            }
        }
        return components to invalid
    }
}

/** Surface and component metadata hold only `extensions`, keyed by Unicode identifiers. */
internal object ExtensionMetadata {
    fun violation(metadata: Map<String, Any?>): Pair<String, String>? {
        metadata.keys.sorted().firstOrNull { it != "extensions" }?.let { key ->
            return "/${pointerEscape(key)}" to "Metadata can only hold 'extensions', not '$key'."
        }
        val raw = metadata["extensions"] ?: return null
        val extensions = A2UIJson.map(raw) ?: return "/extensions" to "'extensions' must be an object."
        extensions.keys.sorted().firstOrNull { !A2UIIdentifier.isValid(it) }?.let { key ->
            return "/extensions/${pointerEscape(key)}" to
                "The extension key '$key' must be a Unicode identifier (UAX #31)."
        }
        return null
    }
}

/** A message from the renderer to the agent, as `renderer_to_agent.json` defines it. */
internal sealed interface RendererMessage {
    /** A person used a component. */
    data class Action(
        val name: String,
        val userMessage: String?,
        val surfaceId: String,
        val sourceComponentId: String,
        val timestamp: String,
        val context: Map<String, Any?>,
        val metadata: Map<String, Any?>? = null,
    ) : RendererMessage

    /**
     * Asks the agent to run a function the renderer doesn't have.
     *
     * @property userActivated Whether a person's gesture started the call, such as a button's
     * action, rather than the surface while it renders. It stays on this device.
     */
    data class CallAgentFunction(
        val surfaceId: String,
        val functionCallId: String,
        val call: FunctionCall,
        val userActivated: Boolean = false,
    ) : RendererMessage

    /** Answers a `callRendererFunction`. */
    data class RendererFunctionResponse(val response: FunctionResponse) : RendererMessage

    /** Reports a problem. */
    data class Error(val error: RendererError) : RendererMessage

    /** The message as JSON, with its version. */
    fun toJson(): Map<String, Any?> = LinkedHashMap<String, Any?>().apply {
        put("version", A2UIProtocol.VERSION)
        when (val message = this@RendererMessage) {
            is Action -> put(
                "action",
                buildMap {
                    put("name", message.name)
                    put("surfaceId", message.surfaceId)
                    put("sourceComponentId", message.sourceComponentId)
                    put("timestamp", message.timestamp)
                    put("context", message.context)
                    message.userMessage?.let { put("userMessage", it) }
                    message.metadata?.let { put("metadata", it) }
                },
            )
            is CallAgentFunction -> put(
                "callAgentFunction",
                mapOf(
                    "surfaceId" to message.surfaceId,
                    "functionCallId" to message.functionCallId,
                    "callFunction" to message.call.toJson(),
                ),
            )
            is RendererFunctionResponse -> put("rendererFunctionResponse", message.response.toJson())
            is Error -> put("error", message.error.toJson())
        }
    }
}

/** A problem the renderer reports to the agent, with one of the spec's codes. */
internal data class RendererError(
    val code: String,
    val message: String,
    val surfaceId: String? = null,
    val path: String? = null,
    val functionCallId: String? = null,
) {
    fun toJson(): Map<String, Any?> = buildMap {
        put("code", code)
        put("message", message)
        surfaceId?.let { put("surfaceId", it) }
        path?.let { put("path", it) }
        functionCallId?.let { put("functionCallId", it) }
    }

    companion object {
        const val VALIDATION_FAILED: String = "VALIDATION_FAILED"
        const val UNALLOWED_PARENT: String = "UNALLOWED_PARENT"
        const val UNALLOWED_CHILD: String = "UNALLOWED_CHILD"
        const val INVALID_FUNCTION_CALL: String = "INVALID_FUNCTION_CALL"
        const val EXECUTION_ERROR: String = "EXECUTION_ERROR"
        const val EXPRESSION_ERROR: String = "EXPRESSION_ERROR"
        const val CATALOG_NOT_FOUND: String = "CATALOG_NOT_FOUND"
        const val INTEGRITY_ERROR: String = "INTEGRITY_ERROR"
        const val DATA_ERROR: String = "DATA_ERROR"
        const val RECURSION_LIMIT_EXCEEDED: String = "RECURSION_LIMIT_EXCEEDED"
        const val TIMEOUT: String = "TIMEOUT"
    }
}
