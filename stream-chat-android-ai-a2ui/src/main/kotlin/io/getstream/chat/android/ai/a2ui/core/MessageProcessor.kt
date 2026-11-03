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

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * The state of one surface: its components, its data and the evaluator its values resolve with.
 */
@Suppress("LongParameterList")
internal class Surface(
    val surfaceId: String,
    val catalogId: String?,
    val sendDataModel: Boolean,
    val metadata: Map<String, Any?>?,
    val dataModel: DataModel,
    val evaluator: Evaluator,
    private val dispatch: (RendererMessage) -> Unit,
) {
    private val componentsById = LinkedHashMap<String, Component>()

    /** The components by id, in the order they first arrived. */
    val components: Map<String, Component> get() = componentsById

    /** The root component, once it has arrived. */
    val root: Component? get() = componentsById[A2UIProtocol.ROOT_ID]

    fun component(id: String): Component? = componentsById[id]

    fun merge(components: List<Component>) {
        components.forEach { componentsById[it.id] = it }
    }

    /** The catalog entry of [component], or `null` when its catalog doesn't define it. */
    fun definition(component: Component): ComponentDefinition? =
        evaluator.catalogs.resolve(component.catalogId, catalogId)?.component(component.type)

    /** Reports a problem of this surface to the agent. */
    fun report(error: RendererError) {
        evaluator.router?.report(error)
    }

    /**
     * Runs [action] of [componentId] in [context]: sends its event to the agent, with its context
     * resolved now, or runs its function as a gesture of the person's.
     *
     * @param metadata Extension metadata of the app's to send with an event.
     * @param userActivation Whether a gesture of the person's started it, which `openUrl` needs.
     */
    fun perform(
        action: ActionDefinition,
        componentId: String,
        context: DataContext,
        metadata: Map<String, Any?>? = null,
        userActivation: Boolean = true,
    ) {
        when (action) {
            is ActionDefinition.Event -> dispatch(
                RendererMessage.Action(
                    name = action.name,
                    userMessage = A2UIJson.string(evaluator.evaluate(action.userMessage, context)),
                    surfaceId = surfaceId,
                    sourceComponentId = componentId,
                    timestamp = timestamp(evaluator.services.now()),
                    context = action.context.mapValues { evaluator.evaluate(it.value, context) },
                    metadata = metadata,
                ),
            )
            is ActionDefinition.Function -> if (userActivation) {
                evaluator.withUserActivation { evaluator.invoke(action.call, context) }
            } else {
                evaluator.invoke(action.call, context)
            }
        }
    }

    /**
     * Runs an action as the agent wrote it, reporting `INVALID_ACTION` when it is malformed, such
     * as an event without a name. See [perform].
     */
    fun performRaw(
        raw: Any?,
        componentId: String,
        context: DataContext,
        metadata: Map<String, Any?>? = null,
        userActivation: Boolean = true,
    ) {
        metadata?.let(ExtensionMetadata::violation)?.let { (pointer, reason) ->
            report(RendererError(RendererError.VALIDATION_FAILED, reason, surfaceId, "/metadata$pointer"))
            return
        }
        if (raw == null) return
        val event = A2UIJson.map(A2UIJson.map(raw)?.get("event"))
        val invalid = when {
            event != null && event["name"] !is String -> "The event of '$componentId' has no 'name'."
            event != null && !isTextValue(event["userMessage"]) ->
                "The 'userMessage' of the event of '$componentId' must be a string."
            else -> null
        }
        val action = ActionDefinition.of(raw)
        if (invalid != null || action == null) {
            val reason = invalid ?: "The action of '$componentId' is neither an event nor a function call."
            report(RendererError(INVALID_ACTION, reason, surfaceId))
            return
        }
        perform(action, componentId, context, metadata, userActivation)
    }

    private companion object {
        const val INVALID_ACTION = "INVALID_ACTION"
    }

    // Whether a raw value can stand for text: absent, a string, a binding or a call.
    private fun isTextValue(raw: Any?): Boolean {
        if (raw == null || raw is String) return true
        val value = DynamicValue.of(raw)
        return value is DynamicValue.Binding || value is DynamicValue.Call
    }

    private fun timestamp(millis: Long): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date(millis))
    }
}

/**
 * Applies A2UI messages to the surfaces of one renderer, and produces the messages that go back
 * to the agent: actions, function calls and responses, and errors.
 *
 * A message that fails doesn't stop the ones after it. Problems that can be found when components
 * arrive are reported with the JSON Pointer of the message field; the renderer still shows what it
 * can, because A2UI arrives while the agent writes it.
 *
 * Confined to one thread, the main thread in an app.
 *
 * @param catalogs The catalogs it supports.
 * @param defaultCatalogId The catalog of a surface that names none.
 * @param services What functions need from the app.
 * @param schedule Runs a block after a delay, for timing out calls to the agent; none by default.
 * @param agentFunctionTimeoutMillis How long a call to the agent may go unanswered.
 * @param send Takes each message for the agent.
 */
@Suppress("TooManyFunctions")
internal class MessageProcessor(
    catalogs: List<Catalog>,
    defaultCatalogId: String? = null,
    val services: RendererServices = RendererServices(),
    private val schedule: ((Long, () -> Unit) -> Unit)? = null,
    private val agentFunctionTimeoutMillis: Long = DEFAULT_AGENT_FUNCTION_TIMEOUT_MILLIS,
    private val send: (RendererMessage) -> Unit,
) : EvaluatorRouter {
    val catalogs: CatalogRegistry = CatalogRegistry(catalogs, defaultCatalogId)

    private val surfacesById = LinkedHashMap<String, Surface>()
    private val remoteCalls = HashMap<RemoteCallKey, RemoteCall>()
    private val remoteCallIds = HashMap<String, RemoteCallKey>()
    private val reported = HashSet<String>()
    private var callCounter = 0
    private val listeners = mutableListOf<() -> Unit>()

    /** The live surfaces, in the order they were created. */
    val surfaces: Map<String, Surface> get() = surfacesById

    fun surface(surfaceId: String): Surface? = surfacesById[surfaceId]

    /** Calls [listener] after every change of a surface's components or data. */
    fun addChangeListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    /** The data of the surfaces that asked for it to go with the person's messages. */
    fun synchronizedDataModel(): Map<String, Any?> =
        surfacesById.filterValues { it.sendDataModel }.mapValues { it.value.dataModel.snapshot }

    /** Removes every surface. */
    fun clear() {
        surfacesById.values.forEach { it.dataModel.dispose() }
        surfacesById.clear()
        remoteCalls.clear()
        remoteCallIds.clear()
        reported.clear()
        changed()
    }

    /**
     * Reads and applies a wire message. One that can't be read is reported: a malformed
     * `callRendererFunction` with `INVALID_FUNCTION_CALL`, anything else with `VALIDATION_FAILED`.
     */
    fun handle(json: Map<String, Any?>) {
        val message = try {
            AgentMessageDecoder.decode(json)
        } catch (error: DecodingException) {
            report(error.rendererError(AgentMessageDecoder.surfaceId(json)))
            return
        }
        handle(message)
    }

    /**
     * Applies what an agent wrote: one message, or a list of them in `{"messages": [...]}`. Each
     * message of a list counts on its own: one that fails doesn't stop the rest.
     */
    fun handleAny(json: Map<String, Any?>) {
        val list = A2UIJson.list(json["messages"])
        if (list != null && !AgentMessageDecoder.isMessage(json)) {
            list.forEach { item ->
                val message = A2UIJson.map(item)
                if (message == null) {
                    report(DecodingException.UnknownMessageType(emptyList()).rendererError(null))
                } else {
                    handle(message)
                }
            }
        } else {
            handle(json)
        }
    }

    /** Checks the whole tree of each surface, once its messages are complete, and reports what is wrong. */
    fun validateTopology(validator: TopologyValidator = TopologyValidator()) {
        surfacesById.values.forEach { surface -> validator.errors(surface).forEach(::report) }
    }

    /** Applies a message. */
    fun handle(message: AgentMessage) {
        when (message) {
            is AgentMessage.CreateSurface -> createSurface(message)
            is AgentMessage.UpdateComponents -> updateComponents(message)
            is AgentMessage.UpdateDataModel -> updateDataModel(message)
            is AgentMessage.DeleteSurface -> deleteSurface(message)
            is AgentMessage.CallRendererFunction -> callRendererFunction(message)
            is AgentMessage.AgentFunctionResponse -> resolveAgentFunction(message.response)
        }
    }

    /** Reports a problem to the agent, once per problem: rendering repeats, the agent's context shouldn't. */
    override fun report(error: RendererError) {
        val key = listOf(error.code, error.message, error.surfaceId.orEmpty(), error.path.orEmpty()).joinToString("|")
        if (reported.add(key)) sendMessage(RendererMessage.Error(error))
    }

    /** Sends a message to the agent. */
    fun sendMessage(message: RendererMessage) {
        send(message)
    }

    private fun changed() {
        listeners.toList().forEach { it() }
    }

    private fun createSurface(message: AgentMessage.CreateSurface) {
        if (surfacesById.containsKey(message.surfaceId)) {
            report(
                RendererError(
                    RendererError.INTEGRITY_ERROR,
                    "Surface '${message.surfaceId}' already exists. Delete it before creating it again.",
                    message.surfaceId,
                ),
            )
            return
        }
        if (message.catalogId != null && catalogs.catalog(message.catalogId) == null) {
            report(
                RendererError(
                    RendererError.CATALOG_NOT_FOUND,
                    "Surface '${message.surfaceId}' requested catalog '${message.catalogId}', which this renderer " +
                        "does not support. Supported catalogs: ${catalogs.supportedCatalogIds.joinToString(", ")}.",
                    message.surfaceId,
                ),
            )
        }
        var initialData: Map<String, Any?>? = message.dataModel
        if (A2UIJson.exceedsDepth(initialData, A2UIProtocol.MAX_NESTING_DEPTH)) {
            report(
                RendererError(
                    RendererError.RECURSION_LIMIT_EXCEEDED,
                    "Global recursion limit exceeded: the data model nests deeper than " +
                        "${A2UIProtocol.MAX_NESTING_DEPTH} levels.",
                    message.surfaceId,
                    "/dataModel",
                ),
            )
            initialData = null
        }
        val dataModel = DataModel(initialData ?: emptyMap<String, Any?>())
        val evaluator = Evaluator(catalogs, message.surfaceId, message.catalogId, services, this)
        val surface = Surface(
            surfaceId = message.surfaceId,
            catalogId = message.catalogId,
            sendDataModel = message.sendDataModel,
            metadata = message.metadata,
            dataModel = dataModel,
            evaluator = evaluator,
            dispatch = ::sendMessage,
        )
        surfacesById[message.surfaceId] = surface
        dataModel.observe(DataPath.Root) { changed() }
        surface.merge(validated(message.components, surface))
        CompositionValidator.errors(surface, message.components).forEach(::report)
        changed()
    }

    private fun updateComponents(message: AgentMessage.UpdateComponents) {
        val surface = surfacesById[message.surfaceId] ?: run {
            report(
                RendererError(
                    RendererError.INTEGRITY_ERROR,
                    "Cannot update components of surface '${message.surfaceId}' because it was not created.",
                    message.surfaceId,
                ),
            )
            return
        }
        surface.merge(validated(message.components, surface))
        CompositionValidator.errors(surface, message.components).forEach(::report)
        changed()
    }

    private fun updateDataModel(message: AgentMessage.UpdateDataModel) {
        val surface = surfacesById[message.surfaceId] ?: run {
            report(
                RendererError(
                    RendererError.INTEGRITY_ERROR,
                    "Cannot update the data model of surface '${message.surfaceId}' because it was not created.",
                    message.surfaceId,
                ),
            )
            return
        }
        if (A2UIJson.exceedsDepth(message.value, A2UIProtocol.MAX_NESTING_DEPTH)) {
            report(
                RendererError(
                    RendererError.RECURSION_LIMIT_EXCEEDED,
                    "Global recursion limit exceeded: the value nests deeper than " +
                        "${A2UIProtocol.MAX_NESTING_DEPTH} levels.",
                    message.surfaceId,
                    "/value",
                ),
            )
            return
        }
        if (message.path.isRoot && message.value != null && A2UIJson.map(message.value) == null) {
            val reason = "The data model root must be an object."
            report(RendererError(RendererError.DATA_ERROR, reason, message.surfaceId, "/value"))
            return
        }
        try {
            surface.dataModel.set(message.path, message.value)
        } catch (error: DataModelException) {
            report(RendererError(RendererError.DATA_ERROR, error.message.orEmpty(), message.surfaceId))
        }
    }

    private fun deleteSurface(message: AgentMessage.DeleteSurface) {
        val surface = surfacesById.remove(message.surfaceId) ?: return
        surface.dataModel.dispose()
        val stale = remoteCalls.keys.filter { it.surfaceId == message.surfaceId }.toSet()
        stale.forEach(remoteCalls::remove)
        remoteCallIds.entries.removeAll { it.value in stale }
        changed()
    }

    // The components of a batch to apply, reporting what can be found wrong before rendering.
    // A batch that repeats an id applies only the first.
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    private fun validated(components: List<Component>, surface: Surface): List<Component> {
        val seen = HashSet<String>()
        val accepted = mutableListOf<Component>()
        val surfaceId = surface.surfaceId
        components.forEachIndexed { index, component ->
            val pointer = "/components/$index"
            if (!seen.add(component.id)) {
                report(
                    RendererError(
                        RendererError.INTEGRITY_ERROR,
                        "Duplicate component ID '${component.id}': it appears more than once in one message.",
                        surfaceId,
                    ),
                )
                return@forEachIndexed
            }
            if (A2UIJson.exceedsDepth(component.properties, A2UIProtocol.MAX_NESTING_DEPTH)) {
                report(
                    RendererError(
                        RendererError.RECURSION_LIMIT_EXCEEDED,
                        "Global recursion limit exceeded: component '${component.id}' nests its properties " +
                            "deeper than " +
                            "${A2UIProtocol.MAX_NESTING_DEPTH} levels.",
                        surfaceId,
                        pointer,
                    ),
                )
                return@forEachIndexed
            }
            component.metadata?.let(ExtensionMetadata::violation)?.let { (path, reason) ->
                report(RendererError(RendererError.VALIDATION_FAILED, reason, surfaceId, "$pointer/metadata$path"))
                return@forEachIndexed
            }
            if (!A2UIIdentifier.isValid(component.id)) {
                report(
                    RendererError(
                        RendererError.VALIDATION_FAILED,
                        "Component id '${component.id}' must be a valid UAX #31 identifier.",
                        surfaceId,
                        "$pointer/id",
                    ),
                )
            }
            for (key in component.properties.keys.sorted()) {
                val calls = DynamicValue.calls(component.properties[key], "$pointer/${pointerEscape(key)}")
                for ((callPointer, call) in calls) {
                    callError(call, callPointer, surface)?.let(::report)
                }
            }
            if (component.hasInvalidCatalogId) {
                report(
                    RendererError(
                        RendererError.VALIDATION_FAILED,
                        "The 'catalogId' of component '${component.id}' must be a string.",
                        surfaceId,
                        "$pointer/catalogId",
                    ),
                )
            }
            val catalog = catalogs.resolve(component.catalogId, surface.catalogId)
            if (catalog == null && !component.hasInvalidCatalogId) {
                report(
                    RendererError(
                        RendererError.CATALOG_NOT_FOUND,
                        (component.catalogId ?: surface.catalogId)?.let { "Catalog not found: $it" }
                            ?: "Component '${component.id}' has no catalogId, and the surface has no default " +
                            "catalogId.",
                        surfaceId,
                        pointer,
                    ),
                )
            }
            val definition = catalog?.component(component.type)
            if (catalog != null && definition != null) {
                propertyErrors(component, definition, catalog, pointer, surfaceId).forEach(::report)
            }
            definition?.childListViolations(component)?.forEach { (path, reason) ->
                report(RendererError(RendererError.VALIDATION_FAILED, reason, surfaceId, "$pointer$path"))
            }
            if (catalog != null && definition == null) {
                report(
                    RendererError(
                        RendererError.VALIDATION_FAILED,
                        "Component type '${component.type}' is not defined by catalog '${catalog.catalogId}'.",
                        surfaceId,
                        "$pointer/component",
                    ),
                )
            }
            for ((key, value) in component.properties) {
                val propertyPointer = "$pointer/${pointerEscape(key)}"
                val violation = DynamicValue.reservedKeyViolation(value)
                when {
                    violation != null ->
                        report(RendererError(RendererError.VALIDATION_FAILED, violation, surfaceId, propertyPointer))
                    DynamicValue.callDepth(value) > A2UIProtocol.MAX_CALL_DEPTH -> report(
                        RendererError(
                            RendererError.VALIDATION_FAILED,
                            "The functionCall depth of '$key' exceeds ${A2UIProtocol.MAX_CALL_DEPTH}.",
                            surfaceId,
                            propertyPointer,
                        ),
                    )
                    else -> DynamicValue.formatStringViolation(value)?.let { (path, reason) ->
                        report(
                            RendererError(RendererError.VALIDATION_FAILED, reason, surfaceId, "$propertyPointer$path"),
                        )
                    }
                }
            }
            accepted.add(component)
        }
        return accepted
    }

    // The properties of [component] its catalog doesn't declare, or that break their schema, and
    // the required ones it lacks.
    private fun propertyErrors(
        component: Component,
        definition: ComponentDefinition,
        catalog: Catalog,
        pointer: String,
        surfaceId: String,
    ): List<RendererError> {
        val errors = mutableListOf<RendererError>()
        definition.required.filter { !component.properties.containsKey(it) }.forEach { property ->
            errors += RendererError(
                RendererError.VALIDATION_FAILED,
                "Component '${component.id}' (${component.type}) is missing the required property '$property'.",
                surfaceId,
                "$pointer/${pointerEscape(property)}",
            )
        }
        for ((key, value) in component.properties) {
            val propertyPointer = "$pointer/${pointerEscape(key)}"
            val schema = definition.properties[key]
            if (schema == null) {
                errors += RendererError(
                    RendererError.VALIDATION_FAILED,
                    "Component '${component.id}' (${component.type}) has no property '$key' in its catalog.",
                    surfaceId,
                    propertyPointer,
                )
                continue
            }
            SchemaValidator.violation(value, schema, catalog, allowsDynamicValues = true)?.let { (path, reason) ->
                errors += RendererError(
                    RendererError.VALIDATION_FAILED,
                    "Property '$key' of component '${component.id}' (${component.type}) $reason.",
                    surfaceId,
                    "$propertyPointer$path",
                )
            }
        }
        return errors
    }

    // A call whose catalog doesn't resolve, or whose literal arguments break its function's schema.
    private fun callError(call: Map<String, Any?>, pointer: String, surface: Surface): RendererError? {
        val name = call[A2UIProtocol.CALL_KEY] as? String ?: return null
        if (name.startsWith(A2UIProtocol.SYSTEM_PREFIX)) return null
        if (call.containsKey("catalogId") && call["catalogId"] !is String) {
            return RendererError(
                RendererError.VALIDATION_FAILED,
                "The 'catalogId' of a call to '$name' must be a string.",
                surface.surfaceId,
                "$pointer/catalogId",
            )
        }
        val catalogId = call["catalogId"] as? String
        val catalog = catalogs.resolve(catalogId, surface.catalogId) ?: return RendererError(
            RendererError.CATALOG_NOT_FOUND,
            (catalogId ?: surface.catalogId)?.let { "Catalog not found: $it" }
                ?: "Function '$name' has no catalogId, and the surface has no default catalogId.",
            surface.surfaceId,
            pointer,
        )
        val function = catalog.function(name) ?: return RendererError(
            RendererError.VALIDATION_FAILED,
            "Unrecognized function '$name': catalog '${catalog.catalogId}' does not define it.",
            surface.surfaceId,
            pointer,
        )
        val schema = function.argumentsSchema ?: return null
        val (path, reason) = SchemaValidator.violation(
            value = A2UIJson.map(call["args"]).orEmpty(),
            schema = schema,
            catalog = catalog,
            allowsDynamicValues = true,
        ) ?: return null
        return RendererError(
            RendererError.VALIDATION_FAILED,
            "Invalid arguments for '$name' at '${path.ifEmpty { "/" }}': $reason.",
            surface.surfaceId,
            "$pointer/args$path",
        )
    }

    @Suppress("ReturnCount")
    private fun callRendererFunction(message: AgentMessage.CallRendererFunction) {
        val call = message.call
        fun reject(reason: String) = respondError(message, RendererError.INVALID_FUNCTION_CALL, reason)
        val catalog = catalogs.catalog(call.catalogId) ?: return reject("Catalog not found: ${call.catalogId}")
        val definition = catalog.function(call.name)
            ?: return reject("Function not found: ${call.name} (catalog '${catalog.catalogId}').")
        if (!definition.isAgentCallable) {
            return reject(
                "Function '${call.name}' cannot be called by agent (allowedCallers is ${definition.allowedCallers}).",
            )
        }
        if (definition.requiresUserActivation) {
            return reject(
                "Function '${call.name}' requires user activation context to execute, which a call from the agent " +
                    "doesn't have.",
            )
        }
        var dataModel = DataModel()
        message.surfaceId?.let { surfaceId ->
            dataModel = surfacesById[surfaceId]?.dataModel ?: return reject("Surface '$surfaceId' does not exist.")
        }
        val evaluator = Evaluator(catalogs, message.surfaceId.orEmpty(), catalog.catalogId, services)
        val context = DataContext(dataModel)
        try {
            val arguments = evaluator.resolveArgumentsStrictly(call.arguments, call.name, context)
            definition.argumentsSchema?.let { schema ->
                SchemaValidator.violation(arguments, schema, catalog)?.let { (_, reason) ->
                    return reject("Invalid arguments for '${call.name}': $reason.")
                }
            }
            val implementation = definition.implementation
                ?: return reject("Function '${call.name}' has no implementation here.")
            val value = implementation(FunctionInvocation(call.name, arguments, context, services, evaluator))
            val response = FunctionResponse(message.functionCallId, value = value)
            sendMessage(RendererMessage.RendererFunctionResponse(response))
        } catch (error: FunctionException) {
            respondError(message, RendererError.EXECUTION_ERROR, error.message.orEmpty())
        } catch (error: ExpressionException) {
            respondError(message, RendererError.EXECUTION_ERROR, error.message.orEmpty())
        } catch (@Suppress("TooGenericExceptionCaught") error: RuntimeException) {
            respondError(message, RendererError.EXECUTION_ERROR, error.message ?: error.toString())
        }
    }

    private fun respondError(message: AgentMessage.CallRendererFunction, code: String, reason: String) {
        val response = FunctionResponse(message.functionCallId, error = FunctionResponse.Failure(code, reason))
        sendMessage(RendererMessage.RendererFunctionResponse(response))
    }

    private fun resolveAgentFunction(response: FunctionResponse) {
        val key = remoteCallIds.remove(response.functionCallId) ?: return
        remoteCalls[key] = if (response.error != null) {
            RemoteCall.Failed(response.error)
        } else {
            RemoteCall.Resolved(response.value)
        }
        changed()
    }

    override fun agentFunctionValue(
        name: String,
        catalogId: String?,
        arguments: Map<String, Any?>,
        surfaceId: String,
    ): Any? {
        val key = RemoteCallKey(surfaceId, name, catalogId, A2UIJson.write(arguments, sortKeys = true))
        return when (val state = remoteCalls[key]) {
            is RemoteCall.Resolved -> state.value
            is RemoteCall.Pending -> null
            is RemoteCall.Failed -> throw FunctionException(state.failure.code, state.failure.message)
            null -> {
                callCounter++
                val callId = "$name-$callCounter"
                remoteCalls[key] = RemoteCall.Pending(callId)
                remoteCallIds[callId] = key
                schedule?.invoke(agentFunctionTimeoutMillis) { timeOut(callId) }
                val call = FunctionCall(name, catalogId, arguments)
                sendMessage(RendererMessage.CallAgentFunction(surfaceId, callId, call))
                null
            }
        }
    }

    private fun timeOut(callId: String) {
        val key = remoteCallIds.remove(callId) ?: return
        val reason = "The agent did not answer the call '$callId' to '${key.name}' in time."
        remoteCalls[key] = RemoteCall.Failed(FunctionResponse.Failure(RendererError.TIMEOUT, reason))
        report(RendererError(RendererError.TIMEOUT, reason, key.surfaceId))
        changed()
    }

    private data class RemoteCallKey(
        val surfaceId: String,
        val name: String,
        val catalogId: String?,
        val arguments: String,
    )

    private sealed interface RemoteCall {
        data class Pending(val callId: String) : RemoteCall
        data class Resolved(val value: Any?) : RemoteCall
        data class Failed(val failure: FunctionResponse.Failure) : RemoteCall
    }

    companion object {
        const val DEFAULT_AGENT_FUNCTION_TIMEOUT_MILLIS: Long = 30_000
    }
}

/**
 * Checks where components are placed against the catalog's `allowedParents` and
 * `allowedChildren`, pointing at the component, or the child slot, of the message that broke the
 * rule.
 */
internal object CompositionValidator {
    // Only the root is checked against `Surface`: a component nothing holds has no parent yet.
    @Suppress("LoopWithTooManyJumpStatements")
    fun errors(surface: Surface, batch: List<Component>): List<RendererError> {
        val batchIndex = HashMap<String, Int>()
        batch.forEachIndexed { index, component -> batchIndex.putIfAbsent(component.id, index) }
        val errors = mutableListOf<RendererError>()
        rootError(surface, batchIndex)?.let(errors::add)
        val parents = surface.components.values.sortedWith(compareBy({ batchIndex[it.id] ?: Int.MAX_VALUE }, { it.id }))
        for (parent in parents) {
            val parentDefinition = surface.definition(parent) ?: continue
            for ((pointer, childId) in parentDefinition.childReferences(parent)) {
                val child = surface.component(childId) ?: continue
                val childDefinition = surface.definition(child) ?: continue
                val parentIndex = batchIndex[parent.id]
                val childIndex = batchIndex[child.id]
                if (parentIndex == null && childIndex == null) continue
                val slot = parentIndex?.let { "/components/$it$pointer" }
                val entry = childIndex?.let { "/components/$it" }
                childDefinition.allowedParents?.takeIf { parent.type !in it }?.let { allowed ->
                    errors += RendererError(
                        RendererError.UNALLOWED_PARENT,
                        "Component '${child.id}' (${child.type}) cannot be placed under parent " +
                            "'${parent.id}' (${parent.type}). " +
                            "Allowed parents: ${list(allowed)}.",
                        surface.surfaceId,
                        entry ?: slot,
                    )
                }
                parentDefinition.allowedChildren?.takeIf { child.type !in it }?.let { allowed ->
                    errors += RendererError(
                        RendererError.UNALLOWED_CHILD,
                        "Container '${parent.id}' (${parent.type}) cannot contain child " +
                            "'${child.id}' (${child.type}). " +
                            "Allowed children: ${list(allowed)}.",
                        surface.surfaceId,
                        slot ?: entry,
                    )
                }
            }
        }
        return errors
    }

    // The root of a batch placed under the surface, which its catalog doesn't allow.
    private fun rootError(surface: Surface, batchIndex: Map<String, Int>): RendererError? {
        val root = surface.root ?: return null
        val index = batchIndex[root.id] ?: return null
        val allowed = surface.definition(root)?.allowedParents ?: return null
        val surfaceType = A2UIProtocol.SURFACE_TYPE
        if (surfaceType in allowed) return null
        return RendererError(
            RendererError.UNALLOWED_PARENT,
            "Component '${root.id}' (${root.type}) cannot be placed under parent '$surfaceType' ($surfaceType). " +
                "Allowed parents: ${list(allowed)}.",
            surface.surfaceId,
            "/components/$index",
        )
    }

    private fun list(names: List<String>): String = names.joinToString(", ", "[", "]") { "'$it'" }
}
