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

/**
 * A structured, LLM-friendly error from the OpenUI pipeline (mirrors `OpenUIError`).
 *
 * Only errors that can be fixed by changing the OpenUI Lang code are reported, which makes them
 * suitable for an automated correction loop (send them back to the model as a follow-up).
 */
public data class OpenUIError internal constructor(
    val source: Source,
    /** Machine-readable code, e.g. `unknown-component`, `missing-required`, `tool-not-found`. */
    val code: String,
    val message: String,
    /** Statement to patch (e.g. `header`). */
    val statementId: String? = null,
    val component: String? = null,
    /** Prop path, e.g. `/title`. */
    val path: String? = null,
    /** Tool name for query/mutation errors. */
    val toolName: String? = null,
    /** Actionable fix context (available components, the correct signature, ...). */
    val hint: String? = null,
    /** Whether the program still renders despite it, such as a `no-root` fallback entry. */
    val isWarning: Boolean = false,
) {
    /**
     * Where an error comes from. [wireName] is the reference implementation's name. A class
     * rather than an enum so new sources can be added without breaking exhaustive `when`s.
     */
    public data class Source(val wireName: String) {
        override fun toString(): String = wireName

        /** The sources the runtime reports. */
        public companion object {
            /** The parser: unknown components, missing props, no root, ... */
            public val Parser: Source = Source("parser")

            /** Evaluation: a prop expression or library function failed. */
            public val Runtime: Source = Source("runtime")

            /** A `Query()` tool call. */
            public val Query: Source = Source("query")

            /** A `Mutation()` tool call. */
            public val Mutation: Source = Source("mutation")
        }
    }

    override fun toString(): String = "${source.wireName}/$code: $message"

    internal companion object {
        /** Converts parser validation errors into errors with hints (port of `enrichErrors`). */
        fun enriched(
            errors: List<ValidationError>,
            schema: OpenUIValue,
            componentNames: List<String>,
        ): List<OpenUIError> = errors.map { error ->
            val hint = when (error.code) {
                ValidationErrorCode.UnknownComponent ->
                    componentNames.takeIf { it.isNotEmpty() }?.let {
                        "Available components: ${it.joinToString(
                            ", ",
                        )}"
                    }
                ValidationErrorCode.MissingRequired, ValidationErrorCode.NullRequired ->
                    signatureHint(error.component, schema["\$defs"][error.component])
                ValidationErrorCode.InlineReserved ->
                    "Declare as a top-level statement: myVar = " +
                        "${error.component}(...)"
                else -> null
            }
            OpenUIError(
                source = Source.Parser,
                code = error.code.wireName,
                message = error.message,
                statementId = error.statementId,
                component = error.component,
                path = error.path.ifEmpty { null },
                hint = hint,
                isWarning = error.isWarning,
            )
        }

        private fun signatureHint(component: String, definition: OpenUIValue?): String? {
            val properties = definition["properties"].objectValue ?: return null
            val required = definition["required"]
                .arrayValue
                ?.mapNotNull { it.stringValue }
                ?.toSet()
                .orEmpty()
            val params = properties.keys.joinToString(", ") { if (it in required) "$it*" else it }
            return "Signature: $component($params) — * marks required"
        }
    }
}
