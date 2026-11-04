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

import java.util.Locale
import java.util.TimeZone

/**
 * A scope of a surface's data model: the root, where every path is absolute, or one item of a
 * list template, where relative paths read the item and `@index` is its position.
 */
internal data class DataContext(val model: DataModel, val scope: DataPath = DataPath.Root, val itemIndex: Int? = null) {

    /** [path] in this scope, as an absolute path. */
    fun resolve(path: DataPath): DataPath = if (path.isAbsolute) path else scope.appending(path)

    /** The value at [path] in this scope, or `null`. */
    fun value(path: DataPath): Any? = try {
        model.get(resolve(path))
    } catch (_: DataModelException) {
        null
    }

    /** Writes [value] at [path] in this scope. */
    fun update(path: DataPath, value: Any?) {
        model.set(resolve(path), value)
    }

    /** The scope of item [index] of the list at [path]. */
    fun item(
        path: DataPath,
        index: Int,
    ): DataContext = DataContext(model, resolve(path).appending(index.toString()), index)
}

/**
 * What the renderer's functions need from the app.
 *
 * @param openUrl Opens a link the person tapped, returning whether it opened.
 * @param canOpenUrl Whether a link may open at all; only web links by default.
 * @param locale The locale numbers and dates are formatted in.
 * @param timeZone The time zone timestamps are shown in.
 * @param now The current time, in milliseconds since the epoch.
 */
internal data class RendererServices(
    val openUrl: (String) -> Boolean = { false },
    val canOpenUrl: (String) -> Boolean = { url ->
        val scheme = url.substringBefore(':', missingDelimiterValue = "").lowercase(Locale.ROOT)
        (scheme == "https" || scheme == "http") && url.startsWith("$scheme://") && url.none { it.isWhitespace() }
    },
    val locale: Locale = Locale.getDefault(),
    val timeZone: TimeZone = TimeZone.getDefault(),
    val now: () -> Long = System::currentTimeMillis,
)

/** Where an evaluator sends what it can't do itself. */
internal interface EvaluatorRouter {
    /**
     * The agent's answer to a call of a function the renderer doesn't have: `null` while the call
     * is on its way, the value once the agent answered. A call a person's gesture started goes out
     * every time, as each gesture asks again.
     *
     * @param userActivated Whether a person's gesture started the call, rather than the surface
     * while it renders.
     * @throws FunctionException When the agent answered with an error, or not in time.
     */
    fun agentFunctionValue(
        name: String,
        catalogId: String?,
        arguments: Map<String, Any?>,
        surfaceId: String,
        userActivated: Boolean = false,
    ): Any?

    /** Reports a problem to the agent. */
    fun report(error: RendererError)
}

/**
 * Evaluates one surface's dynamic values, function calls and checks. Local functions run at once;
 * a function the catalogs don't have goes to the agent and reads `null` until it answers.
 */
internal class Evaluator(
    val catalogs: CatalogRegistry,
    val surfaceId: String,
    val surfaceCatalogId: String?,
    val services: RendererServices = RendererServices(),
    var router: EvaluatorRouter? = null,
) {
    /** How many evaluations failed so far, so a caller can tell whether its own did. */
    var failureCount: Int = 0
        private set

    private var userActivation = 0

    /** What the last failed evaluation reported. */
    var lastFailure: String? = null
        private set

    /** [value] evaluated in [context]: `null` when it is missing or not there yet. */
    fun evaluate(value: DynamicValue, context: DataContext): Any? = when (value) {
        DynamicValue.Missing -> null
        is DynamicValue.Literal -> value.value
        is DynamicValue.Binding -> context.value(value.path)
        is DynamicValue.Call -> invoke(value.call, context)
    }

    /** A raw wire value evaluated in [context], with nested objects and arrays resolved. */
    fun evaluateRaw(raw: Any?, context: DataContext): Any? {
        if (raw is Map<*, *>) {
            return when (val value = DynamicValue.of(raw)) {
                is DynamicValue.Call -> invoke(value.call, context)
                is DynamicValue.Binding -> context.value(value.path)
                // An explicit `null` stays; an entry that resolves to nothing is left out.
                is DynamicValue.Literal -> A2UIJson.map(value.value).orEmpty().mapNotNull { (key, item) ->
                    if (item == null) key to null else evaluateRaw(item, context)?.let { key to it }
                }.toMap()
                DynamicValue.Missing -> null
            }
        }
        if (raw is List<*>) return raw.map { evaluateRaw(it, context) }
        return raw
    }

    /** Runs [call] in [context], with its arguments evaluated first. */
    fun invoke(call: FunctionCall, context: DataContext): Any? =
        invoke(call.name, call.catalogId, resolveArguments(call.arguments, context), context)

    /**
     * Runs a function with evaluated arguments: a system function, else the function of the
     * catalog the call resolves in, else the agent's.
     */
    fun invoke(name: String, catalogId: String?, arguments: Map<String, Any?>, context: DataContext): Any? {
        if (name.startsWith(A2UIProtocol.SYSTEM_PREFIX)) return systemFunction(name, arguments, context)
        val catalog = catalogs.resolve(catalogId, surfaceCatalogId)
            ?: return fail(
                RendererError(
                    RendererError.CATALOG_NOT_FOUND,
                    (catalogId ?: surfaceCatalogId)?.let { "Catalog not found: $it" }
                        ?: "Function '$name' has no catalogId, and the surface has no default catalogId.",
                    surfaceId,
                ),
            )
        val definition = catalog.function(name)
        if (definition != null) {
            if (!definition.isRendererCallable) {
                return fail(
                    RendererError(
                        RendererError.INVALID_FUNCTION_CALL,
                        "Function '$name' is agentOnly and cannot be called by renderer component properties.",
                        surfaceId,
                    ),
                )
            }
            if (definition.requiresUserActivation && userActivation == 0) {
                return fail(
                    RendererError(
                        RendererError.EXPRESSION_ERROR,
                        "Function '$name' requires a user gesture and can only run from an action.",
                        surfaceId,
                    ),
                )
            }
            if (definition.implementation != null) return execute(definition, arguments, context)
        }
        val router = router
        if (router == null) {
            failureCount++
            return null
        }
        return try {
            // The agent learns the catalog the call resolved in, even when the call named none.
            router.agentFunctionValue(name, catalog.catalogId, arguments, surfaceId, userActivation > 0)
        } catch (_: FunctionException) {
            failureCount++
            null
        }
    }

    /** Runs [body] as the handler of a person's gesture, where `openUrl` may run. */
    fun <T> withUserActivation(body: () -> T): T {
        userActivation++
        try {
            return body()
        } finally {
            userActivation--
        }
    }

    /** Runs a function's implementation, reporting a failure to the agent. */
    fun execute(definition: FunctionDefinition, arguments: Map<String, Any?>, context: DataContext): Any? {
        val implementation = definition.implementation ?: return null
        return try {
            implementation(FunctionInvocation(definition.name, arguments, context, services, this))
        } catch (error: FunctionException) {
            fail(RendererError(error.code, error.message.orEmpty(), surfaceId))
        } catch (error: ExpressionException) {
            fail(RendererError(RendererError.EXPRESSION_ERROR, error.message.orEmpty(), surfaceId))
        }
    }

    /**
     * Evaluates each argument for a call from the agent, which answers with an error rather than
     * run with an argument that failed. A binding to nothing reads as `null`.
     */
    fun resolveArgumentsStrictly(
        arguments: Map<String, Any?>,
        function: String,
        context: DataContext,
    ): Map<String, Any?> =
        arguments.keys.sorted().associateWith { key ->
            val failures = failureCount
            val value = evaluateRaw(arguments[key], context)
            if (failureCount != failures) {
                throw FunctionException.invalidArgument(key, function, lastFailure ?: "It could not be evaluated.")
            }
            value
        }

    /** Evaluates the arguments of a call; the ones that read as nothing are left out. */
    fun resolveArguments(arguments: Map<String, Any?>, context: DataContext): Map<String, Any?> =
        arguments.mapNotNull { (key, value) -> evaluateRaw(value, context)?.let { key to it } }.toMap()

    /** Interpolates a `formatString` template in [context]; what isn't there reads as nothing. */
    fun interpolate(template: String, context: DataContext): String =
        ExpressionParser.parseTemplate(template).joinToString("") { segment ->
            when (segment) {
                is TemplateSegment.Text -> segment.text
                is TemplateSegment.Interpolation -> A2UIJson.stringify(evaluate(segment.expression, context))
            }
        }

    /** A parsed template expression evaluated in [context]. */
    fun evaluate(expression: Expression, context: DataContext): Any? = when (expression) {
        is Expression.Literal -> expression.value
        is Expression.Path -> context.value(expression.path)
        is Expression.Call -> invoke(
            expression.name,
            null,
            expression.arguments
                .mapNotNull { (key, argument) -> evaluate(argument, context)?.let { key to it } }
                .toMap(),
            context,
        )
    }

    /**
     * The checks of a component that fail. A condition whose function fails fails; one still
     * waiting on data or on the agent passes, so a form isn't disabled while its data streams in.
     * A check's own message wins over its function's.
     */
    fun failingChecks(
        checks: List<CheckRule>,
        context: DataContext,
    ): List<ValidationResult> = checks.mapNotNull { rule ->
        val failures = failureCount
        val value = evaluate(rule.condition, context)
        if (failureCount != failures) {
            return@mapNotNull ValidationResult(
                isValid = false,
                message = rule.message ?: "This value could not be checked.",
            )
        }
        val result = ValidationResult.of(value)
        if (result.isValid) return@mapNotNull null
        val message = when (val condition = rule.condition) {
            is DynamicValue.Call -> rule.message ?: result.message ?: BasicFunctions.defaultMessage(condition.call.name)
            else -> result.message ?: rule.message
        }
        result.copy(message = message)
    }

    /** Reports [error] and reads as nothing. */
    fun fail(error: RendererError): Any? {
        failureCount++
        lastFailure = error.message
        router?.report(error)
        return null
    }

    private fun systemFunction(name: String, arguments: Map<String, Any?>, context: DataContext): Any? {
        if (name != "@index") {
            return fail(RendererError(RendererError.EXPRESSION_ERROR, "Unknown system function '$name'.", surfaceId))
        }
        val index = context.itemIndex ?: return fail(
            RendererError(
                RendererError.EXPRESSION_ERROR,
                "@index function can only be evaluated inside a collection template iteration scope.",
                surfaceId,
            ),
        )
        val offset = A2UIJson.long(arguments["offset"]) ?: 0L
        return index.toLong() + offset
    }
}
