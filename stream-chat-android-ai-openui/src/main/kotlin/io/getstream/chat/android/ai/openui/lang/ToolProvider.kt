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

package io.getstream.chat.android.ai.openui.lang

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Executes the tools referenced by `Query(...)` and `Mutation(...)` statements.
 *
 * Works with any backend (REST, GraphQL, MCP, local functions). The runtime calls the provider
 * when queries load or refresh, and when an action runs a mutation.
 */
internal fun interface ToolProvider {
    /** Calls tool [name] with [arguments] and returns its data; throw to report a failure. */
    suspend fun callTool(name: String, arguments: OpenUIObject): OpenUIValue

    /**
     * The shortest time between two refreshes of a query. The model writes a query's refresh
     * interval, so a shorter one waits this long. Five seconds by default.
     */
    val minimumRefreshInterval: Duration get() = 5.seconds
}

/** Thrown when a function-map provider has no handler for a tool. */
internal class ToolNotFoundException(
    /** The requested tool. */
    val toolName: String,
    /** The tools the provider has. */
    val availableTools: List<String> = emptyList(),
) : Exception(
    "[openui] No handler for tool \"$toolName\". Available: ${availableTools.joinToString(", ").ifEmpty { "(none)" }}",
)

/** Thrown when an MCP tool call returns `isError: true`. */
internal class McpToolException(public val toolErrorText: String) :
    Exception("MCP tool error: ${toolErrorText.ifEmpty { "Unknown error" }}")

/**
 * A tool provider backed by a map of suspending functions: Kotlin's form of passing a map of tool
 * functions as the tool provider.
 *
 * ```kotlin
 * val tools = FunctionToolProvider(
 *     "search_hotels" to { args -> api.searchHotels(args) },
 * )
 * ```
 */
internal class FunctionToolProvider(
    private val handlers: Map<String, suspend (OpenUIObject) -> OpenUIValue>,
    /** The shortest time between two refreshes of a query. */
    override val minimumRefreshInterval: Duration = 5.seconds,
) : ToolProvider {
    /** A provider with one handler per tool name. */
    constructor(
        vararg handlers: Pair<String, suspend (OpenUIObject) -> OpenUIValue>,
    ) : this(handlers.toMap())

    override suspend fun callTool(name: String, arguments: OpenUIObject): OpenUIValue {
        val handler = handlers[name] ?: throw ToolNotFoundException(name, handlers.keys.toList())
        return handler(arguments)
    }
}

/** The result envelope of an MCP `tools/call` request. */
internal data class McpToolResult(
    val content: List<Content>,
    val structuredContent: OpenUIValue? = null,
    val isError: Boolean = false,
) {
    /** One content block; text blocks have `type` `"text"`. */
    data class Content(val type: String, val text: String? = null)

    /** The tool's data (see [extractToolResult]). */
    internal fun extractedValue(): OpenUIValue {
        val texts = content.filter { it.type == "text" }.map { it.text.orEmpty() }
        if (isError) throw McpToolException(texts.joinToString("\n"))
        if (structuredContent != null && structuredContent !is OpenUIValue.Null) return structuredContent
        if (texts.isEmpty()) return OpenUIValue.Null
        val text = texts.joinToString("")
        return OpenUIJson.parseOrNull(text) ?: OpenUIValue.Str(text)
    }
}

/**
 * The data of an MCP tool call: `structuredContent` when present, else the JSON-parsed text content
 * (falling back to the raw text). Throws [McpToolException] for error results (port of
 * `extractToolResult`).
 */
internal fun extractToolResult(result: McpToolResult): OpenUIValue = result.extractedValue()

/** Minimal MCP client shape (any MCP SDK client can be adapted to it). */
internal fun interface McpClientLike {
    /** Sends a `tools/call` request for [name] with [arguments]. */
    suspend fun callTool(name: String, arguments: OpenUIObject): McpToolResult
}

/** Adapts an [McpClientLike] to [ToolProvider]: Kotlin's form of passing an `McpClientLike` as the tool provider. */
internal class McpToolProvider(public val client: McpClientLike) : ToolProvider {
    override suspend fun callTool(name: String, arguments: OpenUIObject): OpenUIValue =
        client.callTool(name, arguments).extractedValue()
}
