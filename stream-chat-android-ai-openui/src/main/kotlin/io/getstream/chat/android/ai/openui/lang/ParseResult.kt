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
 * A resolved component invocation (mirrors `ElementNode`).
 *
 * The parser converts OpenUI Lang text into a tree of these nodes. Positional arguments are
 * mapped to named [props] in the order the library schema declares them.
 */
public data class ElementNode internal constructor(
    /** Component name as defined in the library, e.g. `Table`. */
    val typeName: String,
    /** Named props. Values may contain nested elements and runtime expressions. */
    val props: OpenUIObject,
    /** True while the parser hasn't received all tokens for this node (streaming). */
    val partial: Boolean = false,
    /** False when every prop is a static literal, so evaluation can be skipped. `null` counts as dynamic. */
    val hasDynamicProps: Boolean? = null,
    /** Source statement name (`header` in `header = CardHeader(...)`); `null` for inline components. */
    val statementId: String? = null,
) {
    /** The JSON shape used by the reference implementation. */
    internal val jsonValue: OpenUIValue
        get() = OpenUIValue.obj(
            "type" to OpenUIValue.Str("element"),
            "typeName" to OpenUIValue.Str(typeName),
            "props" to OpenUIValue.Obj(props.mapValues { it.plainJson }),
            "partial" to OpenUIValue.of(partial),
            "hasDynamicProps" to hasDynamicProps?.let { OpenUIValue.of(it) },
            "statementId" to statementId?.let { OpenUIValue.Str(it) },
        )
}

/**
 * A machine-readable validation error code, such as `missing-required`. A class rather than an
 * enum so new codes can be added without breaking exhaustive `when`s.
 */
public data class ValidationErrorCode(
    /** The code as the reference implementation writes it, e.g. `missing-required`. */
    val wireName: String,
) {
    override fun toString(): String = wireName

    /** The codes the parser reports. */
    public companion object {
        /** A required prop is missing. */
        public val MissingRequired: ValidationErrorCode = ValidationErrorCode("missing-required")

        /** A required prop is `null`. */
        public val NullRequired: ValidationErrorCode = ValidationErrorCode("null-required")

        /** A call to a component the library doesn't have. */
        public val UnknownComponent: ValidationErrorCode = ValidationErrorCode("unknown-component")

        /** `Query()` or `Mutation()` used inline instead of as a statement. */
        public val InlineReserved: ValidationErrorCode = ValidationErrorCode("inline-reserved")

        /** More positional args than the component has params. */
        public val ExcessArgs: ValidationErrorCode = ValidationErrorCode("excess-args")

        /** A value that doesn't match the prop's schema. */
        public val TypeMismatch: ValidationErrorCode = ValidationErrorCode("type-mismatch")

        /** An `@Name(...)` call that is not a built-in or one of the library's functions or actions. */
        public val UnknownFunction: ValidationErrorCode = ValidationErrorCode("unknown-function")

        /** No entry statement, or one that doesn't render a component. */
        public val NoRoot: ValidationErrorCode = ValidationErrorCode("no-root")
    }
}

/**
 * A prop validation error. Components with missing required props are dropped from the output
 * tree and their errors are recorded here.
 */
public data class ValidationError internal constructor(
    val code: ValidationErrorCode,
    /** Component type name, e.g. `Header`. */
    val component: String,
    /** JSON Pointer path within the props, e.g. `/title`, or empty. */
    val path: String,
    /** Human (and LLM) readable message. */
    val message: String,
    /** Statement that triggered the error. */
    val statementId: String? = null,
    /** Whether the program still renders despite it, such as a `no-root` fallback entry. */
    val isWarning: Boolean = false,
) {
    /** The JSON shape used by the reference implementation. */
    internal val jsonValue: OpenUIValue
        get() = OpenUIValue.obj(
            "code" to OpenUIValue.Str(code.wireName),
            "component" to OpenUIValue.Str(component),
            "path" to OpenUIValue.Str(path),
            "message" to OpenUIValue.Str(message),
            "statementId" to statementId?.let { OpenUIValue.Str(it) },
            "severity" to if (isWarning) OpenUIValue.Str("warning") else null,
        )
}

/** A `Query(tool, args, defaults, refresh)` statement, with its arguments kept as AST. */
internal data class QueryStatementInfo(
    val statementId: String,
    val toolAST: ASTNode?,
    val argsAST: ASTNode?,
    val defaultsAST: ASTNode?,
    val refreshAST: ASTNode?,
    /** `$variables` referenced by the args, extracted at parse time. */
    val deps: List<String>?,
    /** False while the `Query()` call is still streaming. */
    val complete: Boolean = true,
) {
    /** The JSON shape used by the reference implementation. */
    internal val jsonValue: OpenUIValue
        get() = OpenUIValue.obj(
            "statementId" to OpenUIValue.Str(statementId),
            "toolAST" to (toolAST?.jsonValue ?: OpenUIValue.Null),
            "argsAST" to (argsAST?.jsonValue ?: OpenUIValue.Null),
            "defaultsAST" to (defaultsAST?.jsonValue ?: OpenUIValue.Null),
            "refreshAST" to (refreshAST?.jsonValue ?: OpenUIValue.Null),
            "deps" to deps?.let { names -> OpenUIValue.Arr(names.map { OpenUIValue.Str(it) }) },
            "complete" to OpenUIValue.of(complete),
        )
}

/** A `Mutation(tool, args)` statement, with its arguments kept as AST. */
internal data class MutationStatementInfo(
    val statementId: String,
    val toolAST: ASTNode?,
    val argsAST: ASTNode?,
) {
    /** The JSON shape used by the reference implementation. */
    internal val jsonValue: OpenUIValue
        get() = OpenUIValue.obj(
            "statementId" to OpenUIValue.Str(statementId),
            "toolAST" to (toolAST?.jsonValue ?: OpenUIValue.Null),
            "argsAST" to (argsAST?.jsonValue ?: OpenUIValue.Null),
        )
}

/**
 * The output of one parse (mirrors `ParseResult`).
 *
 * While streaming, each chunk produces a new result; [root] progressively resolves from `null`
 * to a partial tree to the complete tree.
 */
public data class ParseResult internal constructor(
    /** The root element, or `null` if nothing can be rendered yet. */
    val root: ElementNode?,
    /** Set when the entry picks a component at runtime, such as `root = $dark ? a : b`. */
    internal val rootExpression: ASTNode? = null,
    val meta: Meta,
    /** `$variable` declarations with their materialized default values. */
    val stateDeclarations: OpenUIObject,
    /** `Query()` statements, with their arguments kept as AST. */
    internal val queryStatements: List<QueryStatementInfo>,
    /** `Mutation()` statements, with their arguments kept as AST. */
    internal val mutationStatements: List<MutationStatementInfo>,
) {
    /** Parse diagnostics. */
    public data class Meta internal constructor(
        /** True when the input was truncated (unclosed strings or brackets). */
        val incomplete: Boolean,
        /** References used but not (yet) defined. */
        val unresolved: List<String>,
        /** Value statements not reachable from the root (excludes `$state`, `Query` and `Mutation`). */
        val orphaned: List<String>,
        /** Number of `identifier = expression` statements parsed. */
        val statementCount: Int,
        val errors: List<ValidationError>,
    )

    /** The JSON shape used by the reference implementation. */
    internal val jsonValue: OpenUIValue
        get() = OpenUIValue.obj(
            "root" to (root?.jsonValue ?: OpenUIValue.Null),
            "rootExpression" to rootExpression?.jsonValue,
            "meta" to OpenUIValue.obj(
                "incomplete" to OpenUIValue.of(meta.incomplete),
                "unresolved" to OpenUIValue.Arr(meta.unresolved.map { OpenUIValue.Str(it) }),
                "orphaned" to OpenUIValue.Arr(meta.orphaned.map { OpenUIValue.Str(it) }),
                "statementCount" to OpenUIValue.of(meta.statementCount),
                "errors" to OpenUIValue.Arr(meta.errors.map { it.jsonValue }),
            ),
            "stateDeclarations" to OpenUIValue.Obj(stateDeclarations.mapValues { it.plainJson }),
            "queryStatements" to OpenUIValue.Arr(queryStatements.map { it.jsonValue }),
            "mutationStatements" to OpenUIValue.Arr(mutationStatements.map { it.jsonValue }),
        )

    internal companion object {
        fun empty(incomplete: Boolean = true) = ParseResult(
            root = null,
            meta = Meta(incomplete, emptyList(), emptyList(), 0, emptyList()),
            stateDeclarations = OpenUIObject.Empty,
            queryStatements = emptyList(),
            mutationStatements = emptyList(),
        )
    }
}

/** One positional parameter of a component or call, compiled from the library's JSON Schema. */
internal data class ParamDef(
    /** Parameter name, e.g. `title`. */
    val name: String,
    val required: Boolean,
    /** The schema `default` (`null` when there is none; [OpenUIValue.Null] for `default: null`). */
    val defaultValue: OpenUIValue? = null,
    /** The raw JSON Schema fragment for this param. */
    val schema: OpenUIValue? = null,
)

/**
 * A call the runtime evaluates, with its positional parameters: a built-in or library function,
 * or an action step.
 */
internal data class CallDef(
    val kind: Kind,
    val params: List<ParamDef>,
    /** Built-ins coerce their arguments, so they aren't checked against the parameters. */
    val isBuiltin: Boolean = false,
    /** Lazy calls (`@Each`, `Action`, `@Run`, `@Set`, `@Reset`) get their arguments unevaluated. */
    val isLazy: Boolean = false,
) {
    /** Whether a call computes a value or is an action step. */
    enum class Kind {
        /** Computes a value, like `@Count(rows)`. */
        Function,

        /** A step of an action, like `@Set($page, 2)`. */
        Action,
    }
}

/**
 * Component name → positional params, compiled from a library JSON Schema, plus the calls
 * programs can make: the built-in functions and steps, and the library's own functions and
 * actions.
 */
internal class ParamMap(
    /** Component name → ordered params, in library order. */
    public val components: Map<String, List<ParamDef>>,
    /** The built-in functions and steps, and the library's own functions and actions. */
    public val callDefs: Map<String, CallDef> = Builtins.calls,
) {
    /** A map of [components] with only the built-in calls. */
    public constructor(vararg components: Pair<String, List<ParamDef>>) : this(components.toMap())

    /** The params of component [name], or `null` when the library has no such component. */
    public operator fun get(name: String): List<ParamDef>? = components[name]

    /** Whether the library has component [name]. */
    public operator fun contains(name: String): Boolean = name in components

    /** Component names in library order. */
    public val names: List<String> get() = components.keys.toList()

    override fun equals(other: Any?): Boolean =
        other is ParamMap && components == other.components && callDefs == other.callDefs

    override fun hashCode(): Int = 31 * components.hashCode() + callDefs.hashCode()
}
