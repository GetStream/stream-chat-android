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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

/**
 * Runs the A2UI 1.0 conformance suite (`conformance/core` of github.com/a2ui-project/a2ui),
 * one test per case. Cases for other protocol versions and for what a renderer doesn't do are
 * skipped with the reason in [ConformanceSkips].
 */
@RunWith(Parameterized::class)
internal class ConformanceTest(
    @Suppress("unused") private val suite: String,
    private val name: String,
    private val case: Map<String, Any?>,
) {

    @Test
    fun `conformance case`() {
        val reason = ConformanceSkips.reason(suite, name, case)
        assumeTrue(reason.orEmpty(), reason == null)
        when (val action = case["action"]) {
            "validate", "process_messages" -> runProcessMessages()
            "evaluate_function" -> runEvaluateFunction()
            "parse_expression_template" -> runParseExpression()
            "data_model" -> runDataModel()
            "resolve_path" -> runResolvePath()
            "handle_rpc" -> runHandleRpc()
            "dispatch_action" -> runDispatchAction()
            "get_renderer_data_model" -> runRendererDataModel()
            else -> fail("No runner for action $action")
        }
    }

    // region Messages

    private fun runProcessMessages() {
        val harness = harness()
        val steps = A2UIJson.list(case["steps"])?.mapNotNull(A2UIJson::map) ?: listOf(case)
        steps.forEachIndexed { index, step ->
            val messages = messagesOf(step) ?: return@forEachIndexed
            val isLast = index == steps.lastIndex
            val expectError = step["expectError"] ?: step["expect_error"] ?: case["expectError"].takeIf { isLast }
            if (expectError != null) {
                harness.sent.clear()
                harness.process(messages)
                harness.processor.surfaces.values.forEach(::resolveComponents)
                topology()?.let { validator -> harness.processor.validateTopology(validator) }
                ErrorExpectations.assertMatches(expectError, harness.errors)
            } else {
                harness.process(messages)
                topology()?.let { validator ->
                    touchedSurfaces(messages).mapNotNull(harness.processor::surface).forEach { surface ->
                        val errors = validator.errors(surface)
                        assertTrue("Unexpected tree errors: ${errors.map { it.message }}", errors.isEmpty())
                    }
                }
                val expected = step["expect"] ?: case["expect"].takeIf { isLast }
                expected?.let { assertSurfaces(harness, it) }
            }
        }
    }

    // The checks of whole trees the case asks for: a `validate` case is strict unless it says
    // otherwise, a `process_messages` case only when it says so.
    private fun topology(): TopologyValidator? {
        A2UIJson.map(case["validationConfig"])?.let { flags ->
            return TopologyValidator(
                allowOrphans = flags["allowOrphanComponents"] == true,
                allowDangling = flags["allowDanglingReferences"] == true,
                allowMissingRoot = flags["allowMissingRoot"] == true,
            )
        }
        val strict = if (case["action"] == "validate") case["strictMode"] != false else case["strictMode"] == true
        return if (strict) TopologyValidator() else null
    }

    private fun touchedSurfaces(messages: List<Any?>): Set<String> = messages.flatMap { message ->
        val map = A2UIJson.map(message) ?: return@flatMap emptyList()
        A2UIJson.list(map["messages"])?.let { return@flatMap touchedSurfaces(it) }
        listOf("createSurface", "updateComponents").mapNotNull { key ->
            A2UIJson.map(map[key])?.takeIf { it.containsKey("components") }?.get("surfaceId") as? String
        }
    }.toSet()

    private fun messagesOf(step: Map<String, Any?>): List<Any?>? {
        val raw = step["messages"] ?: step["payload"] ?: step["message"]?.let(::listOf) ?: return null
        return A2UIJson.list(raw) ?: listOf(raw)
    }

    @Suppress("LoopWithTooManyJumpStatements")
    private fun assertSurfaces(harness: Harness, expected: Any?) {
        val surfaces = A2UIJson.map(A2UIJson.map(expected)?.get("surfaces")) ?: return
        for ((surfaceId, raw) in surfaces) {
            val expectation = A2UIJson.map(raw) ?: continue
            val surface = harness.processor.surface(surfaceId)
            if (expectation["exists"] == false) {
                assertNull("Surface $surfaceId should not exist", surface)
                continue
            }
            assertNotNull("Surface $surfaceId should exist", surface)
            surface!!
            expectation["sendDataModel"]?.let { assertEquals(it, surface.sendDataModel) }
            if (expectation.containsKey("dataModel")) {
                assertTrue(
                    "dataModel: expected ${A2UIJson.write(expectation["dataModel"])}, " +
                        "got ${A2UIJson.write(surface.dataModel.snapshot)}",
                    A2UIJson.isEqual(expectation["dataModel"], surface.dataModel.snapshot),
                )
            }
            if (expectation.containsKey("metadata")) {
                assertTrue(A2UIJson.isEqual(expectation["metadata"], surface.metadata))
            }
            val resolved = resolveComponents(surface)
            expectation["components"]?.let { components -> assertComponents(surface, resolved, components) }
            for (field in listOf("validationResult", "validationResults", "isValid", "validationErrors")) {
                val byComponent = A2UIJson.map(expectation[field]) ?: continue
                for ((componentId, value) in byComponent) {
                    val node = resolved[componentId] ?: failWith("Component $componentId is missing from $surfaceId")
                    assertContains(value, node.properties[field], "$componentId.$field")
                }
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun assertComponents(surface: Surface, resolved: Map<String, Resolved>, expected: Any) {
        val entries = A2UIJson.map(expected)?.toList()
            ?: A2UIJson.list(expected).orEmpty().mapNotNull(A2UIJson::map).map { (it["id"] as String) to it }
        for ((id, raw) in entries) {
            val component = surface.component(id)
            val node = resolved[id]
            assertTrue("Component $id is missing", component != null || node != null)
            val expectation = A2UIJson.map(raw) ?: continue
            (expectation["component"] as? String)
                ?.let { assertEquals("type of $id", it, component?.type ?: node?.type) }
            if (expectation.containsKey("metadata")) {
                assertTrue(A2UIJson.isEqual(expectation["metadata"], component?.metadata))
            }
            if (node != null) {
                for ((key, value) in expectation) {
                    if (key == "id" || key == "component" || key == "metadata") continue
                    assertContains(value, node.properties[key], "$id.$key")
                }
            }
        }
        if (A2UIJson.list(expected) != null && entries.all { surface.component(it.first) != null }) {
            assertEquals("component count", entries.size, surface.components.size)
        }
    }

    private fun runRendererDataModel() {
        val harness = harness()
        val steps = A2UIJson.list(case["steps"])?.mapNotNull(A2UIJson::map) ?: listOf(case)
        steps.forEach { step -> messagesOf(step)?.let(harness::process) }
        val version = A2UIJson.map(case["args"])?.get("version")
        val actual = if (version == A2UIProtocol.VERSION) {
            mapOf("version" to A2UIProtocol.VERSION, "surfaces" to harness.processor.synchronizedDataModel())
        } else {
            null
        }
        if (case["expect"] == null) assertNull(actual) else assertContains(case["expect"], actual)
    }

    // endregion

    // region Functions

    private fun runEvaluateFunction() {
        val function = case["function"] as String
        val args = A2UIJson.map(case["args"]).orEmpty()
        val locale = (case["locale"] as? String)?.let(Locale::forLanguageTag) ?: Locale.US
        val surfaceBlock = A2UIJson.map(case["surface"])
        val errors = mutableListOf<RendererError>()
        val router = object : EvaluatorRouter {
            override fun agentFunctionValue(
                name: String,
                catalogId: String?,
                arguments: Map<String, Any?>,
                surfaceId: String,
            ): Any? {
                errors += RendererError(RendererError.EXPRESSION_ERROR, "Unrecognized function '$name'", surfaceId)
                return null
            }

            override fun report(error: RendererError) {
                errors += error
            }
        }
        val catalogs = if (surfaceBlock != null) {
            catalogPaths().filterNotNull()
        } else {
            listOf(BasicCatalog.catalog) + catalogPaths().filterNotNull()
        }
        val surfaceCatalogId =
            if (surfaceBlock != null) surfaceBlock["catalogId"] as? String else BasicCatalog.catalog.catalogId
        val evaluator = Evaluator(
            CatalogRegistry(catalogs),
            "main",
            surfaceCatalogId,
            Harness.testServices().copy(locale = locale),
            router,
        )
        val context = DataContext(DataModel(case["dataModel"] ?: emptyMap<String, Any?>()))
        val call = buildMap {
            put(A2UIProtocol.CALL_KEY, function)
            (case["catalogId"] as? String)?.let { put("catalogId", it) }
            put("args", args)
        }
        val value = try {
            // Without a surface, the case calls the function's implementation directly, as a gesture would.
            if (surfaceBlock == null) {
                evaluator.withUserActivation { evaluator.evaluateRaw(call, context) }
            } else {
                evaluator.evaluateRaw(call, context)
            }
        } catch (error: FunctionException) {
            errors += RendererError(error.code, error.message.orEmpty())
            null
        }
        val expectError = case["expectError"]
        if (expectError != null) {
            ErrorExpectations.assertMatches(expectError, errors)
            return
        }
        assertTrue("Unexpected errors: ${errors.map { it.toJson() }}", errors.isEmpty())
        val expected = case["expect"]
        val actual = if (expected is Boolean) A2UIJson.map(value)?.get("valid") ?: value else value
        if (expected == null) assertNull(actual) else assertContains(expected, actual)
    }

    private fun runParseExpression() {
        val input = case["input"] as String
        val expectError = case["expect_error"] ?: case["expectError"]
        val segments = try {
            ExpressionParser.parseTemplate(input)
        } catch (error: ExpressionException) {
            if (expectError == null) throw error
            ErrorExpectations.assertMatches(
                expectError,
                listOf(RendererError(RendererError.EXPRESSION_ERROR, error.message.orEmpty())),
            )
            return
        }
        if (expectError != null) fail("Expected a parse error, got ${segments.map(::segmentJson)}")
        val parts = mutableListOf<Any?>()
        for (segment in segments.map(::segmentJson)) {
            if (segment is String && parts.lastOrNull() is String) {
                parts[parts.lastIndex] = parts.last() as String + segment
            } else if (segment != "") {
                parts.add(segment)
            }
        }
        assertContains(case["expect"], parts)
    }

    private fun segmentJson(segment: TemplateSegment): Any? = when (segment) {
        is TemplateSegment.Text -> segment.text
        is TemplateSegment.Interpolation -> expressionJson(segment.expression)
    }

    private fun expressionJson(expression: Expression): Any? = when (expression) {
        is Expression.Literal -> expression.value
        is Expression.Path -> mapOf("path" to expression.source)
        is Expression.Call -> mapOf(
            "call" to expression.name,
            "args" to expression.arguments.mapValues { expressionJson(it.value) },
            "returnType" to "any",
        )
    }

    // endregion

    // region Data model

    @Suppress("NestedBlockDepth")
    private fun runDataModel() {
        val model = DataModel(case["initial"] ?: emptyMap<String, Any?>())
        val notified = mutableListOf<String>()
        A2UIJson.list(case["watch"]).orEmpty().filterIsInstance<String>().forEach { path ->
            model.observe(DataPath.parse(path)) { notified.add(path) }
        }
        for (step in A2UIJson.list(case["steps"]).orEmpty().mapNotNull(A2UIJson::map)) {
            notified.clear()
            val path = DataPath.parse(step["path"] as? String ?: "")
            val expectError = step["expect_error"] ?: step["expectError"]
            try {
                when (step["op"]) {
                    "get" -> {
                        val value = model.get(path)
                        if (step["expect_absent"] == true) assertNull("${step["path"]}", value)
                        if (step.containsKey("expect")) assertContains(step["expect"], value, "${step["path"]}")
                        when (step["expect_type"]) {
                            "list" -> assertTrue(value is List<*>)
                            "object" -> assertTrue(value is Map<*, *>)
                        }
                    }
                    "set" -> model.set(path, step["value"])
                    "delete" -> model.set(path, null)
                    "dispose" -> model.dispose()
                }
                if (expectError != null) fail("Expected an error for ${A2UIJson.write(step)}")
            } catch (error: DataModelException) {
                if (expectError == null) throw error
                ErrorExpectations.assertMatches(
                    expectError,
                    listOf(RendererError(RendererError.DATA_ERROR, error.message.orEmpty())),
                )
            }
            A2UIJson.list(step["expect_notified"])?.let { expected ->
                assertEquals(expected.map { it.toString() }.sorted(), notified.sorted())
            }
            A2UIJson.map(step["expect_values"])?.forEach { (watched, value) ->
                assertContains(value, model.get(DataPath.parse(watched)), watched)
            }
        }
    }

    private fun runResolvePath() {
        val args = A2UIJson.map(case["args"]).orEmpty()
        val context = DataContext(DataModel(), DataPath.parse(args["contextPath"] as? String ?: "/"))
        assertEquals(case["expect"], context.resolve(DataPath.parse(args["path"] as String)).toString())
    }

    // endregion

    // region Function calls between agent and renderer

    @Suppress("LongMethod")
    private fun runHandleRpc() {
        val args = A2UIJson.map(case["args"]).orEmpty()
        val functionMetadata = A2UIJson.map(args["functionMetadata"]).orEmpty()
        args["uiBindingScenario"]?.let { return runAgentBinding(A2UIJson.map(it)!!) }
        args["uiActionFallbackScenario"]?.let { return runAgentActionFallback(A2UIJson.map(it)!!) }
        val message = A2UIJson.map(args["message"]) ?: failWith("Unsupported rpc case")
        val callCatalogId = A2UIJson.map(A2UIJson.map(message["callRendererFunction"])?.get("callFunction"))
            ?.get("catalogId") as? String
        val functions = functionMetadata.mapValues { (name, raw) ->
            val metadata = A2UIJson.map(raw).orEmpty()
            FunctionDefinition(
                name = name,
                argumentsSchema = A2UIJson.map(metadata["parameters"]),
                allowedCallers = metadata["allowedCallers"] as? String ?: FunctionDefinition.RENDERER_ONLY,
                requiresUserActivation = metadata["requiresUserActivation"] == true,
                implementation = RpcFunctions.implementation(name),
            )
        }
        val catalogs = mutableListOf(BasicCatalog.catalog, BasicCatalog.catalog.copy(catalogId = "basic"))
        if (callCatalogId != null && callCatalogId != "non_existent_catalog") {
            val base = catalogs.firstOrNull { it.catalogId == callCatalogId } ?: Catalog(callCatalogId)
            catalogs.removeAll { it.catalogId == callCatalogId }
            catalogs += base.copy(functions = base.functions + functions)
        }
        val harness = Harness(catalogs)
        (args["surfaceId"] as? String)?.let { surfaceId ->
            harness.process(
                listOf(
                    mapOf(
                        "version" to "1.0",
                        "createSurface" to mapOf(
                            "surfaceId" to surfaceId,
                            "catalogId" to "basic",
                            "dataModel" to (args["dataModel"] ?: emptyMap<String, Any?>()),
                        ),
                    ),
                ),
            )
        }
        harness.sent.clear()
        harness.process(listOf(message))
        val expect = A2UIJson.map(case["expect"])
        val responses = harness.sent.filterIsInstance<RendererMessage.RendererFunctionResponse>()
        if (expect != null && expect.containsKey("response")) {
            val expected = expect["response"]
            if (expected == null) {
                assertTrue(responses.isEmpty())
            } else {
                assertEquals(1, responses.size)
                val actual = responses.single().toJson()
                val expectedResponse = A2UIJson.map(A2UIJson.map(expected)?.get("rendererFunctionResponse"))!!
                val actualResponse = A2UIJson.map(actual["rendererFunctionResponse"])!!
                assertEquals(expectedResponse["functionCallId"], actualResponse["functionCallId"])
                if (expectedResponse.containsKey("value")) {
                    assertContains(expectedResponse["value"], actualResponse["value"], "value")
                }
                A2UIJson.map(expectedResponse["error"])?.let { error ->
                    val actualError = A2UIJson.map(actualResponse["error"])
                        ?: failWith("Expected an error, got ${A2UIJson.write(actual)}")
                    assertEquals(error["code"], actualError["code"])
                    val wanted = error["message"] as String
                    assertTrue(
                        "'$wanted' in '${actualError["message"]}'",
                        ErrorExpectations.messageMatches(wanted, actualError["message"] as String),
                    )
                }
            }
        }
        A2UIJson.map(expect?.get("error"))?.let { error -> ErrorExpectations.assertMatches(error, harness.errors) }
        case["expectError"]?.let { ErrorExpectations.assertMatches(it, harness.errors) }
    }

    private fun runAgentBinding(scenario: Map<String, Any?>) {
        val catalogId = scenario["catalogId"] as String
        val harness = Harness(listOf(BasicCatalog.catalog, Catalog(catalogId, BasicCatalog.catalog.components)))
        val surfaceId = scenario["surfaceId"] as String
        harness.process(
            listOf(
                mapOf("version" to "1.0", "createSurface" to mapOf("surfaceId" to surfaceId, "catalogId" to catalogId)),
                mapOf(
                    "version" to "1.0",
                    "updateComponents" to mapOf(
                        "surfaceId" to surfaceId,
                        "components" to listOf(scenario["component"]),
                    ),
                ),
            ),
        )
        val surface = harness.processor.surface(surfaceId)!!
        fun text() = NodeContext(surface, surface.rootNode()!!).value("text")
        val expect = A2UIJson.map(case["expect"])!!
        assertEquals(expect["initialValue"], text())
        val call = harness.sent.filterIsInstance<RendererMessage.CallAgentFunction>().single()
        assertContains(expect["outboundCall"], call.toJson())
        harness.processor.handle(
            mapOf(
                "version" to "1.0",
                "agentFunctionResponse" to mapOf(
                    "functionCallId" to call.functionCallId,
                    "value" to scenario["inboundResponseValue"],
                ),
            ),
        )
        assertEquals(expect["resolvedValue"], text())
    }

    private fun runAgentActionFallback(scenario: Map<String, Any?>) {
        val catalogId = scenario["catalogId"] as String
        val harness = Harness(listOf(BasicCatalog.catalog, Catalog(catalogId, BasicCatalog.catalog.components)))
        val surfaceId = scenario["surfaceId"] as String
        harness.process(
            listOf(
                mapOf(
                    "version" to "1.0",
                    "createSurface" to mapOf("surfaceId" to surfaceId, "catalogId" to catalogId),
                ),
            ),
        )
        val surface = harness.processor.surface(surfaceId)!!
        val action = ActionDefinition.of(scenario["actionPayload"]) as ActionDefinition.Function
        val context = DataContext(surface.dataModel)
        surface.perform(action, "button", context)
        val call = harness.sent.filterIsInstance<RendererMessage.CallAgentFunction>().single()
        val expect = A2UIJson.map(case["expect"])!!
        assertContains(expect["outboundCall"], call.toJson())
        harness.processor.handle(
            mapOf(
                "version" to "1.0",
                "agentFunctionResponse" to mapOf(
                    "functionCallId" to call.functionCallId,
                    "value" to scenario["inboundResponseValue"],
                ),
            ),
        )
        assertContains(expect["result"], surface.evaluator.invoke(action.call, context))
    }

    // endregion

    // region Actions

    @Suppress("LongMethod", "NestedBlockDepth")
    private fun runDispatchAction() {
        val surfaceId = case["surfaceId"] as? String ?: "main_surface"
        val localFunctions = mapOf<String, FunctionImplementation>(
            "incrementCounter" to { invocation ->
                val path = DataPath.parse(invocation.requireString("path"))
                val current = A2UIJson.double(invocation.context.value(path)) ?: 0.0
                invocation.context.update(path, (current + (invocation.double("step") ?: 1.0)).toLong())
                null
            },
            "failingAction" to { throw FunctionException(RendererError.EXECUTION_ERROR, "The action failed.") },
        ).map { (name, implementation) -> FunctionDefinition(name, implementation = implementation) }
        val catalog = BasicCatalog.catalog.let {
            it.copy(functions = it.functions + localFunctions.associateBy { function -> function.name })
        }
        val harness = Harness(listOf(catalog))
        harness.process(
            listOf(
                mapOf(
                    "version" to "1.0",
                    "createSurface" to mapOf(
                        "surfaceId" to surfaceId,
                        "catalogId" to catalog.catalogId,
                        "dataModel" to (case["dataModel"] ?: emptyMap<String, Any?>()),
                    ),
                ),
            ),
        )
        val surface = harness.processor.surface(surfaceId)!!
        val scope = DataPath.parse(case["scope"] as? String ?: "/")
        val index = scope.segments.lastOrNull()?.let(DataModel::arrayIndex)
        val context = DataContext(surface.dataModel, scope, index)
        val sourceComponentId = case["sourceComponentId"] as? String ?: "button"
        harness.sent.clear()
        surface.performRaw(
            raw = case["actionPayload"],
            componentId = sourceComponentId,
            context = context,
            metadata = A2UIJson.map(case["metadata"]),
            userActivation = case["userActivation"] != false,
        )
        val dispatched = harness.sent.filterIsInstance<RendererMessage.Action>()
        case["expectError"]?.let { ErrorExpectations.assertMatches(it, harness.errors) }
        if (case.containsKey("expectDispatched")) {
            val expected = A2UIJson.map(case["expectDispatched"])
            if (expected == null) {
                assertTrue("Nothing should be dispatched: $dispatched", dispatched.isEmpty())
            } else {
                val action = A2UIJson.map(dispatched.single().toJson()["action"])!!
                for ((key, value) in expected) {
                    when (key) {
                        "timestampIso8601" -> assertTrue(
                            Regex("^\\d{4}-\\d{2}-\\d{2}T").containsMatchIn(action["timestamp"] as String),
                        )
                        else -> if (value == null) assertNull(action[key]) else assertContains(value, action[key], key)
                    }
                }
            }
        }
        A2UIJson.map(case["expectDataModel"])?.let { assertContains(it, surface.dataModel.snapshot) }
    }

    // endregion

    // region Catalogs

    private fun harness(): Harness = Harness(catalogsFor(case))

    private fun catalogPaths(): List<Catalog?> =
        A2UIJson.list(case["catalogPaths"]).orEmpty().filterIsInstance<String>().map { path ->
            when {
                path == "catalogs/basic/v1/catalog.json" -> BasicCatalog.catalog
                path.startsWith("conformance/test_data/catalogs/") && path.endsWith("_v1_0.json") ->
                    Catalog.fromJson(
                        A2UIJson.map(Fixtures.json("conformance/catalogs/${path.substringAfterLast('/')}"))!!,
                    )
                // A catalog of another protocol version isn't available to a 1.0 surface.
                else -> null
            }
        }

    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    private fun catalogsFor(case: Map<String, Any?>): List<Catalog> {
        val basic = BasicCatalog.catalog
        val catalogs = LinkedHashMap<String, Catalog>()
        catalogs[basic.catalogId] = basic
        catalogs["basic"] = basic.copy(catalogId = "basic")
        fun inline(spec: Map<String, Any?>, fallbackId: String): Catalog {
            val catalogId = spec["catalogId"] as? String ?: fallbackId
            return if (spec.containsKey("components") || spec.containsKey("functions")) {
                Catalog.fromJson(spec + ("catalogId" to catalogId))
            } else {
                Catalog(catalogId, basic.components)
            }
        }
        A2UIJson.map(A2UIJson.map(case["catalog"])?.get("catalogSchema"))?.let { schema ->
            val catalog = Catalog.fromJson(schema)
            catalogs[catalog.catalogId] = catalog
        }
        A2UIJson.map(case["catalog"])?.takeIf { it.containsKey("components") || it.containsKey("catalogId") }?.let {
            val catalog = inline(it, "catalog-${case["name"]}")
            catalogs[catalog.catalogId] = catalog
        }
        A2UIJson.list(case["catalogs"]).orEmpty().mapNotNull(A2UIJson::map).filter { spec ->
            // A catalog of another protocol version isn't available to a 1.0 surface.
            (spec["protocolVersion"] ?: A2UIJson.map(spec["catalogSchema"])?.get("protocolVersion")) !in OTHER_VERSIONS
        }.forEach { spec ->
            (spec["catalogSchema"] as? Map<*, *>)?.let { schema ->
                val catalog = Catalog.fromJson(A2UIJson.map(schema)!!)
                catalogs[catalog.catalogId] = catalog
            } ?: run {
                if (spec.containsKey("catalogId")) inline(spec, "").let { catalogs[it.catalogId] = it }
            }
        }
        A2UIJson.list(case["catalogPaths"]).orEmpty().filterIsInstance<String>().forEach { path ->
            if (path.startsWith("conformance/test_data/catalogs/") && path.endsWith("_v1_0.json")) {
                val document = A2UIJson.map(Fixtures.json("conformance/catalogs/${path.substringAfterLast('/')}"))!!
                val catalog = Catalog.fromJson(document)
                catalogs[catalog.catalogId] = catalog
                catalogs.putIfAbsent("test-catalog", catalog.copy(catalogId = "test-catalog"))
            }
        }
        // A surface that names an unknown catalog gets one with the basic components, unless the
        // case is about that catalog being missing.
        val expectsCatalogError = A2UIJson.map(case["expectError"])?.get("category") == "CatalogError"
        if (!expectsCatalogError) {
            createSurfaceCatalogIds(case).forEach { id -> catalogs.putIfAbsent(id, Catalog(id, basic.components)) }
        }
        return catalogs.values.toList()
    }

    private fun createSurfaceCatalogIds(value: Any?): List<String> = when (value) {
        is Map<*, *> -> {
            val own = A2UIJson.map(value["createSurface"])?.get("catalogId") as? String
            listOfNotNull(own) + value.values.flatMap(::createSurfaceCatalogIds)
        }
        is List<*> -> value.flatMap(::createSurfaceCatalogIds)
        else -> emptyList()
    }

    // endregion

    companion object {
        private val OTHER_VERSIONS = setOf("v0.8", "v0.9", "v0.9.1")

        private val SUITES = listOf(
            "expressions",
            "functions",
            "data_model",
            "data_context",
            "data_deletion",
            "data_model_pointers",
            "index_function",
            "reserved_keys",
            "validation_result",
            "error_responses",
            "message_processor_v1_0",
            "validator_v1_0",
            "composition_constraints",
            "rpc_functions",
            "actions",
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}: {1}")
        fun cases(): List<Array<Any>> = SUITES.flatMap { suite ->
            Fixtures.cases(suite).map { case -> arrayOf(suite, case["name"] as String, case) }
        }
    }
}

/** The functions the suite's `handle_rpc` cases declare, as the reference harnesses implement them. */
internal object RpcFunctions {
    fun implementation(name: String): FunctionImplementation = when (name) {
        "playMedia" -> { _ -> mapOf("playing" to true, "timestamp" to 0L) }
        "openExternalUrl" -> { _ -> mapOf("opened" to true) }
        "failingFunction" -> { _ ->
            throw FunctionException(RendererError.EXECUTION_ERROR, "An error occurred during function execution.")
        }
        "echoArgs" -> { invocation -> invocation.arguments }
        else -> { _ -> null }
    }
}
