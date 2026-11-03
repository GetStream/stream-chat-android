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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import java.util.Locale
import java.util.TimeZone

/** Reads the test resources. */
internal object Fixtures {
    fun text(path: String): String = Fixtures::class.java.classLoader!!.getResourceAsStream(path)
        ?.bufferedReader()?.use { it.readText() }
        ?: error("Missing test resource $path")

    fun json(path: String): Any? = A2UIJson.parse(text(path))

    @Suppress("UNCHECKED_CAST")
    fun cases(suite: String): List<Map<String, Any?>> = json("conformance/core/$suite.json") as List<Map<String, Any?>>
}

/** A renderer under test, with everything it sent to the agent. */
internal class Harness(
    catalogs: List<Catalog>,
    defaultCatalogId: String? = null,
    services: RendererServices = testServices(),
) {
    val sent = mutableListOf<RendererMessage>()
    val processor = MessageProcessor(catalogs, defaultCatalogId, services, send = { sent.add(it) })

    val errors: List<RendererError> get() = sent.filterIsInstance<RendererMessage.Error>().map { it.error }

    fun process(messages: List<Any?>) {
        messages.forEach { message ->
            val json = A2UIJson.map(A2UIJson.normalize(message))
            if (json == null) {
                sent.add(RendererMessage.Error(DecodingException.UnknownMessageType(emptyList()).rendererError(null)))
            } else {
                processor.handleAny(json)
            }
        }
    }

    companion object {
        fun testServices(openUrl: (String) -> Boolean = { true }) = RendererServices(
            openUrl = openUrl,
            locale = Locale.US,
            timeZone = TimeZone.getTimeZone("UTC"),
            now = { 0L },
        )
    }
}

/** What a component of a surface resolves to: its type and its properties, evaluated. */
internal data class Resolved(val type: String, val properties: Map<String, Any?>)

/**
 * Resolves every component of [surface] at the root scope, keyed by id, and every component a
 * list template repeats, keyed by `<id>_<index>`, as the conformance harnesses do.
 */
internal fun resolveComponents(surface: Surface): Map<String, Resolved> {
    val resolved = LinkedHashMap<String, Resolved>()

    @Suppress("LoopWithTooManyJumpStatements")
    fun resolve(component: Component, context: DataContext, key: String, visiting: Set<String>) {
        if (key in resolved || component.id in visiting) return
        val definition = surface.definition(component)
        val node = Node(component, context, key, depth = 0)
        val nodeContext = NodeContext(surface, node)
        val properties = LinkedHashMap<String, Any?>()
        for ((name, raw) in component.properties) {
            val schema = definition?.properties?.get(name)
            val reference = schema?.get("\$ref") as? String
            val isChildReference = reference?.endsWith("/ChildList") == true || reference?.endsWith("/Child") == true ||
                reference?.endsWith("/ComponentId") == true || name == "child" || name == "children"
            properties[name] = when {
                isChildReference -> raw
                name == "checks" || name == "action" -> raw
                else -> surface.evaluator.evaluateRaw(raw, context)
            }
        }
        val failing = nodeContext.failingChecks()
        properties["validationResults"] = failing.map { it.toJson() }
        properties["validationResult"] = (failing.firstOrNull() ?: ValidationResult.Valid).toJson()
        properties["isValid"] = failing.none { it.severity == ValidationResult.SEVERITY_ERROR }
        properties["validationErrors"] = failing
            .filter { it.severity == ValidationResult.SEVERITY_ERROR }
            .mapNotNull { it.message ?: it.code }
        resolved[key] = Resolved(component.type, properties)

        val index = context.itemIndex
        val references = definition?.childReferences(component).orEmpty().map { it.second }
        for (property in component.properties.values) {
            val template = ChildList.of(property) as? ChildList.Template ?: continue
            val templateComponent = surface.component(template.componentId) ?: continue
            val items = A2UIJson.list(context.value(template.path)).orEmpty()
            items.indices.forEach { item ->
                resolve(
                    templateComponent,
                    context.item(template.path, item),
                    "${template.componentId}_$item",
                    visiting + component.id,
                )
            }
        }
        if (index != null) {
            references.forEach { id ->
                surface.component(id)?.let { resolve(it, context, "${id}_$index", visiting + component.id) }
            }
        }
    }
    surface.components.values.forEach { resolve(it, DataContext(surface.dataModel), it.id, emptySet()) }
    return resolved
}

/** The conformance suite's error expectations, matched against what the renderer reported or threw. */
internal object ErrorExpectations {

    private val CATEGORY_CODES = mapOf(
        "ValidationError" to setOf(
            RendererError.VALIDATION_FAILED,
            RendererError.UNALLOWED_PARENT,
            RendererError.UNALLOWED_CHILD,
            RendererError.INVALID_FUNCTION_CALL,
            RendererError.EXPRESSION_ERROR,
            RendererError.INTEGRITY_ERROR,
            RendererError.RECURSION_LIMIT_EXCEEDED,
            RendererError.CATALOG_NOT_FOUND,
            "INVALID_ACTION",
        ),
        "CatalogError" to setOf(RendererError.CATALOG_NOT_FOUND),
        "IntegrityError" to setOf(RendererError.INTEGRITY_ERROR, RendererError.RECURSION_LIMIT_EXCEEDED),
        "RecursionError" to setOf(RendererError.RECURSION_LIMIT_EXCEEDED),
        "DataError" to setOf(RendererError.DATA_ERROR),
        "ExpressionError" to setOf(
            RendererError.EXPRESSION_ERROR,
            RendererError.EXECUTION_ERROR,
            RendererError.INVALID_FUNCTION_CALL,
        ),
        "ParseError" to setOf(RendererError.VALIDATION_FAILED, RendererError.EXPRESSION_ERROR),
    )

    /** Asserts that one of [errors] meets [expected]: a message, or the suite's structured form. */
    fun assertMatches(expected: Any?, errors: List<RendererError>) {
        assertTrue("Expected an error matching ${A2UIJson.write(expected)}, but none was reported", errors.isNotEmpty())
        val failures = errors.map { error -> mismatch(expected, error) }
        if (failures.none { it == null }) {
            fail(
                "No reported error matches ${A2UIJson.write(expected)}:\n" +
                    errors.zip(failures).joinToString("\n") { (error, reason) ->
                        "  ${A2UIJson.write(error.toJson())} -> $reason"
                    },
            )
        }
    }

    // Why [error] doesn't meet [expected], or `null` when it does.
    @Suppress("CyclomaticComplexMethod", "ReturnCount", "NestedBlockDepth")
    private fun mismatch(expected: Any?, error: RendererError): String? {
        if (expected is String) return if (messageMatches(expected, error.message)) null else "message"
        val expectation = A2UIJson.map(expected) ?: return null
        (expectation["category"] as? String)?.let { category ->
            val codes = CATEGORY_CODES[category]
            if (codes != null && error.code !in codes) return "category $category"
        }
        (expectation["code"] as? String)?.let { if (error.code != it) return "code" }
        (expectation["message"] as? String)?.let { if (!messageMatches(it, error.message)) return "message" }
        (expectation["path"] as? String)?.let { if (!pathMatches(it, error)) return "path" }
        (expectation["surfaceId"] as? String)?.let { if (error.surfaceId != it) return "surfaceId" }
        (expectation["functionCallId"] as? String)?.let { if (error.functionCallId != it) return "functionCallId" }
        A2UIJson.list(expectation["details"])?.forEach { detail ->
            val map = A2UIJson.map(detail) ?: return@forEach
            // Lower-case codes name a reference validator's own checks, not the protocol's.
            (map["code"] as? String)?.takeIf { it.uppercase() == it }
                ?.let { if (error.code != it) return "details.code" }
            // A detail's path may be relative to the component it is about.
            (map["path"] as? String)
                ?.let { if (error.path?.endsWith(it) != true && !it.contains('.')) return "details.path" }
        }
        A2UIJson.map(expectation["rendererError"])?.let { rendererError ->
            val envelope = A2UIJson.map(rendererError["error"]) ?: rendererError
            for (key in listOf("code", "surfaceId", "functionCallId", "path")) {
                if (key == "path" && envelope.containsKey(key)) {
                    if (!pathMatches(envelope[key] as String, error)) return "rendererError.path"
                } else if (envelope.containsKey(key) && error.toJson()[key] != envelope[key]) {
                    return "rendererError.$key"
                }
            }
            (envelope["message"] as? String)
                ?.let { if (!messageMatches(it, error.message)) return "rendererError.message" }
            if (envelope.containsKey("functionCallId") && !envelope.containsKey("surfaceId") &&
                error.surfaceId != null
            ) {
                return "rendererError.surfaceId must be absent"
            }
        }
        return null
    }

    // A missing required property is reported at the property; some SDKs report it at the
    // component, which the suite's reference harness accepts as well.
    private fun pathMatches(expected: String, error: RendererError): Boolean =
        error.path == expected ||
            "missing the required property" in error.message && error.path?.substringBeforeLast('/') == expected

    /** Whether an emitted message says what the suite expects, in this renderer's words. */
    fun messageMatches(expected: String, actual: String): Boolean {
        val wanted = expected.lowercase().replace('"', '\'')
        val got = actual.lowercase().replace('"', '\'')
        if (wanted in got) return true
        if (runCatching { Regex(expected).containsMatchIn(actual) }.getOrDefault(false)) return true
        return "not of type" in wanted && ("must be" in got || "not of type" in got) ||
            "is a required property" in wanted && "required" in got ||
            "additional properties are not allowed" in wanted && "additional properties" in got ||
            "circular" in wanted && ("circular" in got || "self-reference" in got) ||
            "orphan" in wanted && "not reachable" in got
    }
}

/**
 * Asserts that [actual] holds what [expected] states: every expected key, recursively, with numbers compared by
 * value.
 */
internal fun assertContains(expected: Any?, actual: Any?, path: String = "") {
    val expectedMap = A2UIJson.map(expected)
    if (expectedMap != null) {
        val actualMap = A2UIJson.map(actual)
            ?: failWith("$path: expected an object ${A2UIJson.write(expected)}, got ${A2UIJson.write(actual)}")
        for ((key, value) in expectedMap) {
            if (value == null) {
                assertTrue(
                    "$path/$key: expected nothing, got ${A2UIJson.write(actualMap?.get(key))}",
                    actualMap?.get(key) == null,
                )
            } else {
                assertContains(value, actualMap?.get(key), "$path/$key")
            }
        }
        return
    }
    val expectedList = A2UIJson.list(expected)
    if (expectedList != null) {
        val actualList = A2UIJson.list(actual)
            ?: failWith("$path: expected a list ${A2UIJson.write(expected)}, got ${A2UIJson.write(actual)}")
        assertEquals("$path: list size of ${A2UIJson.write(actual)}", expectedList.size, actualList.size)
        expectedList.indices.forEach { assertContains(expectedList[it], actualList[it], "$path/$it") }
        return
    }
    val matches = A2UIJson.isEqual(expected, actual) ||
        A2UIJson.isNumber(actual) && expected is String && A2UIJson.stringify(actual) == expected
    assertTrue("$path: expected ${A2UIJson.write(expected)}, got ${A2UIJson.write(actual)}", matches)
}

/** Fails the test with [message]. */
internal fun failWith(message: String): Nothing = throw AssertionError(message)
