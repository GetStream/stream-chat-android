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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A two-way bound form field (port of `resolveStateField`). */
public class StateField internal constructor(
    /** The field name (the `$variable` name for reactive fields). */
    public val name: String,
    /** Current value (`null` when unset). */
    public val value: OpenUIValue?,
    /** Whether the field is bound to a `$variable`. */
    public val isReactive: Boolean,
    private val setter: (OpenUIValue?) -> Unit,
) {
    /** Writes [newValue] to the bound `$variable` or form field. */
    public fun setValue(newValue: OpenUIValue?) {
        setter(newValue)
    }
}

/**
 * Owns the state of one rendered OpenUI response: incremental parsing, `$variables` and form
 * state, `Query`/`Mutation` execution, prop evaluation and action handling.
 *
 * This is the counterpart of the React renderer's `useOpenUIState`. Feed it the response text
 * as it streams with [update] and render [state]. Keep one runtime per message so state
 * survives list recycling in chats.
 *
 * The runtime is confined to one thread: create and call it on the UI thread, and give it a
 * [scope] that dispatches there (tool calls suspend; they don't block it).
 *
 * @param library The component library the model was prompted with.
 * @param toolProvider Executes `Query`/`Mutation` tools.
 * @param initialState Previously persisted state (from [onStateUpdate]). `$`-prefixed keys
 *   restore bindings; other keys restore form state.
 */
internal class OpenUIRuntime(
    /** The component library the model was prompted with. */
    val library: LibrarySchema,
    scope: CoroutineScope,
    toolProvider: ToolProvider? = null,
    initialState: OpenUIObject? = null,
) {
    /** Everything a renderer observes. */
    data class State(
        /** The raw response text. */
        val response: String? = null,
        /** Whether the model is still streaming (inputs disabled, queries wait for the end). */
        val isStreaming: Boolean = false,
        /** The raw parse result (runtime expressions unevaluated). */
        val parseResult: ParseResult? = null,
        /** The evaluated root element to render. */
        val root: ElementNode? = null,
        /** Whether any query is fetching. */
        val isQueryLoading: Boolean = false,
        /** Structured errors (empty while streaming). */
        val errors: List<OpenUIError> = emptyList(),
    )

    /** The program's `$variables` and form state. */
    val store: OpenUIStore = OpenUIStore()

    /** Runs the program's `Query()` and `Mutation()` tools. */
    val queryManager: QueryManager = QueryManager(toolProvider, scope)

    /**
     * The program's `$variables` (as `"$name"` keys) and form fields (under their form name), for
     * UIs to observe. Change them through [setFieldValue] or a [StateField].
     */
    val storeSnapshot: StateFlow<OpenUIObject> get() = store.snapshot

    private val mutableState = MutableStateFlow(State())

    /** Everything a renderer observes; updates on every [update], state change and query result. */
    val state: StateFlow<State> = mutableState.asStateFlow()

    /** The current [State.response]. */
    val response: String? get() = mutableState.value.response

    /** The current [State.isStreaming]. */
    val isStreaming: Boolean get() = mutableState.value.isStreaming

    /** The current [State.parseResult]. */
    val parseResult: ParseResult? get() = mutableState.value.parseResult

    /** The current [State.root]. */
    val root: ElementNode? get() = mutableState.value.root

    /** The current [State.isQueryLoading]. */
    val isQueryLoading: Boolean get() = mutableState.value.isQueryLoading

    /** The current [State.errors]. */
    val errors: List<OpenUIError> get() = mutableState.value.errors

    /**
     * Conversation and navigation actions (`@ToAssistant`, `@OpenUrl`, follow-ups, buttons without
     * actions), and the library's custom actions.
     */
    var onAction: ((ActionEvent) -> Unit)? = null

    /** Called on every user-driven state change with the full state snapshot (persist it as is). */
    var onStateUpdate: ((OpenUIObject) -> Unit)? = null

    /** Called with structured errors when streaming ends (and with an empty list when they clear). */
    var onError: ((List<OpenUIError>) -> Unit)? = null

    private val streamParser = library.streamParser()
    private var initialState: OpenUIObject? = initialState
    private var storeInitKey: String? = null
    private var isInitializingStore = false
    private var lastErrorKey = ""
    private var isRecomputing = false
    private var needsRecompute = false
    private var runtimeErrors: List<OpenUIError> = emptyList()
    private val functions = library.functions.associateBy { it.name }
    private val customActions = library.customActions.associateBy { it.name }

    init {
        store.onChange = ::storeDidChange
        queryManager.onChange = ::queryManagerDidChange
    }

    private fun mutate(transform: (State) -> State) {
        mutableState.value = transform(mutableState.value)
    }

    // Input --------------------------------------------------------------------------------

    /** Feeds the latest response text (the full text so far, not a delta). */
    fun update(response: String?, isStreaming: Boolean) {
        val current = mutableState.value
        val responseChanged = response != current.response
        val streamingChanged = isStreaming != current.isStreaming
        if (!responseChanged && !streamingChanged && current.parseResult != null) return

        var parseResult = current.parseResult
        // The end of the stream completes a `$name` that was still streaming in.
        if (responseChanged || streamingChanged || parseResult == null) {
            parseResult = if (!response.isNullOrEmpty()) streamParser.set(response, complete = !isStreaming) else null
        }
        mutate { it.copy(response = response, isStreaming = isStreaming, parseResult = parseResult) }
        initializeStoreIfNeeded()
        runQueryEffects()
        recompute()
        collectErrors()
    }

    /** Restores persisted state (e.g. when re-rendering a stored message). */
    fun restore(state: OpenUIObject) {
        initialState = state
        storeInitKey = null
        initializeStoreIfNeeded()
        recompute()
    }

    /** Reactivates query execution after [dispose]. */
    fun activate() {
        queryManager.activate()
        runQueryEffects()
    }

    /** Stops refresh timers and pending work. */
    fun dispose() {
        queryManager.dispose()
    }

    // Evaluation ---------------------------------------------------------------------------

    /** Context used to evaluate expressions against the current state and query results. */
    val evaluationContext: EvaluationContext
        get() = EvaluationContext(
            getState = { name -> unwrapFieldValue(store[name]) },
            resolveRef = { name -> queryManager.mutationResult(name) ?: queryManager.result(name) },
            functions = functions,
            actions = customActions,
        )

    /** Evaluates an expression against the current state. */
    fun evaluate(node: ASTNode): OpenUIValue? = Evaluator.evaluate(node, evaluationContext)

    private fun recompute() {
        if (isRecomputing) {
            needsRecompute = true
            return
        }
        isRecomputing = true
        try {
            var passes = 0
            do {
                needsRecompute = false
                passes++
                val (evaluated, errors) =
                    PropEvaluator.evaluateRoot(parseResult, evaluationContext, library.reactiveProps)
                runtimeErrors = errors
                val loading = queryManager.snapshot.value.loading
                    .isNotEmpty()
                mutate { it.copy(root = evaluated, isQueryLoading = loading) }
            } while (needsRecompute && passes < MAX_RECOMPUTE_PASSES)
        } finally {
            isRecomputing = false
        }
    }

    private fun initializeStoreIfNeeded() {
        val result = parseResult
        if (result == null && initialState == null) return
        val declarations = result?.stateDeclarations ?: OpenUIObject.Empty
        val key = OpenUIJson.stringify(OpenUIValue.Obj(declarations)) + "::" +
            OpenUIJson.stringify(initialState?.let { OpenUIValue.Obj(it) } ?: OpenUIValue.Null)
        if (key == storeInitKey) return
        storeInitKey = key

        isInitializingStore = true
        try {
            val bindingDefaults = OpenUIObject.Builder()
            for ((name, value) in initialState.orEmpty()) {
                if (name.startsWith("$")) bindingDefaults[name] = value else store[name] = value
            }
            store.initialize(declarations, bindingDefaults.build())
        } finally {
            isInitializingStore = false
        }
    }

    private fun runQueryEffects() {
        if (isStreaming) return
        val context = evaluationContext
        val snapshot = store.snapshot.value
        val result = parseResult

        val queryNodes = result?.queryStatements.orEmpty().map { statement ->
            val deps = statement.deps?.takeIf { it.isNotEmpty() }?.let { names ->
                OpenUIValue.Obj(
                    OpenUIObject.build { for (name in names) this[name] = snapshot[name] ?: OpenUIValue.Null },
                )
            }
            QueryManager.QueryNode(
                statementId = statement.statementId,
                toolName = statement.toolAST
                    ?.let { Evaluator.evaluate(it, context) }
                    ?.let(JS::toString)
                    .orEmpty(),
                args = statement.argsAST?.let { Evaluator.evaluate(it, context) } ?: OpenUIValue.Null,
                defaults = statement.defaultsAST?.let { Evaluator.evaluate(it, context) } ?: OpenUIValue.Null,
                deps = deps,
                refreshInterval = statement.refreshAST?.let { Evaluator.evaluate(it, context) }?.let(JS::toNumber),
                complete = statement.complete,
            )
        }
        queryManager.evaluateQueries(queryNodes)

        queryManager.registerMutations(
            result?.mutationStatements.orEmpty().map { statement ->
                QueryManager.MutationNode(
                    statementId = statement.statementId,
                    toolName = statement.toolAST
                        ?.let { Evaluator.evaluate(it, context) }
                        ?.let(JS::toString)
                        .orEmpty(),
                )
            },
        )
    }

    private fun storeDidChange(key: String) {
        if (key.startsWith("$")) {
            runQueryEffects()
            recompute()
        }
        if (!isInitializingStore) onStateUpdate?.invoke(store.snapshot.value)
    }

    private fun queryManagerDidChange() {
        recompute()
        collectErrors()
    }

    private fun collectErrors() {
        if (isStreaming) {
            if (errors.isNotEmpty()) mutate { it.copy(errors = emptyList()) }
            if (lastErrorKey.isNotEmpty()) {
                lastErrorKey = ""
                onError?.invoke(emptyList())
            }
            return
        }
        val collected = ArrayList<OpenUIError>()
        val result = parseResult
        // A missing or unusable entry is reported by the parser as `no-root`.
        if (!response.isNullOrEmpty() && (result?.meta?.statementCount ?: 0) == 0) {
            collected += OpenUIError(
                source = OpenUIError.Source.Parser,
                code = "parse-failed",
                message = "Response could not be parsed as valid openui-lang",
                hint = "The entire response must be valid openui-lang code starting with root = " +
                    "${library.root ?: "Root"}(...)",
            )
        }
        result?.meta?.errors?.takeIf { it.isNotEmpty() }?.let {
            collected += OpenUIError.enriched(it, library.jsonSchema, library.componentNames)
        }
        collected += runtimeErrors
        collected += queryManager.snapshot.value.errors

        val key = collected.joinToString("\n") + "#${collected.size}"
        if (key == lastErrorKey) return
        lastErrorKey = key
        mutate { it.copy(errors = collected) }
        onError?.invoke(collected)
    }

    // Form state ---------------------------------------------------------------------------

    /** Reads a field value: top level for `$bindings`, nested under `formName` for form fields. */
    fun fieldValue(formName: String?, name: String): OpenUIValue? {
        if (formName == null) return unwrapFieldValue(store[name])
        return unwrapFieldValue(store[formName].objectValue?.get(name))
    }

    /** Writes a field value as `{ value, componentType }`. */
    fun setFieldValue(formName: String?, componentType: String?, name: String, value: OpenUIValue?) {
        val wrapped = OpenUIValue.obj(
            "value" to (value ?: OpenUIValue.Null),
            "componentType" to componentType?.let { OpenUIValue.Str(it) },
        )
        if (formName == null) {
            store[name] = wrapped
            return
        }
        val form = store[formName].objectValue ?: OpenUIObject.Empty
        store[formName] = OpenUIValue.Obj(form.with(name, wrapped))
    }

    /** Resolves a field that may be bound to a `$variable` (reactive) or stored as form state. */
    fun stateField(
        name: String,
        binding: OpenUIValue?,
        formName: String?,
        componentType: String? = null,
    ): StateField {
        val target = ReactiveBinding.target(binding)
        if (target != null) {
            val expression = ReactiveBinding.expression(binding)
            return StateField(name, store[target], isReactive = true) { newValue ->
                val context = evaluationContext.copy(extraScope = mapOf("\$value" to (newValue ?: OpenUIValue.Null)))
                store[target] = expression?.let { Evaluator.evaluate(it, context) } ?: newValue
            }
        }
        val current = fieldValue(formName, name)
        val value = if (JS.isNullish(current)) binding ?: current else current
        return StateField(
            name,
            value,
            isReactive = false,
        ) { newValue -> setFieldValue(formName, componentType, name, newValue) }
    }

    /** The form state included with actions: `{ formName: fields }`, or the whole state. */
    fun formPayload(formName: String?): OpenUIObject {
        if (formName != null) {
            val form = store[formName]
            if (form is OpenUIValue.Obj) return OpenUIObject.of(formName to form)
        }
        return store.snapshot.value
    }

    // Actions ------------------------------------------------------------------------------

    /**
     * Runs an action: a plan of steps (one step, or several in `Action([...])`, halting when a
     * mutation fails), a legacy action object (`{ type, params }` / `{ type: "open_url", url }`),
     * or, when [action] is `null`, a continue-conversation event carrying [label]. Each step reads
     * the form state when it runs, so an earlier `@Set` in the same plan is visible.
     */
    suspend fun triggerAction(label: String, formName: String? = null, action: OpenUIValue? = null) {
        val legacy = action.objectValue
        if (legacy != null && "steps" !in legacy) {
            val type =
                legacy["type"]?.takeIf(JS::isTruthy)?.let(JS::toString) ?: BuiltinActionType.CONTINUE_CONVERSATION
            // Only a real call delivers a custom action; a legacy object can't pose as one.
            if (type in customActions) return
            val params = OpenUIObject
                .Builder()
                .apply {
                    legacy["params"].objectValue?.forEach { (key, value) -> this[key] = value }
                    legacy["url"]?.takeIf(JS::isTruthy)?.let { this["url"] = it }
                    legacy["context"]?.takeIf(JS::isTruthy)?.let { this["context"] = it }
                }.build()
            onAction?.invoke(ActionEvent(type, params, label, formPayload(formName), formName))
            return
        }
        if (!ActionPlan.isPlan(action)) {
            onAction?.invoke(
                ActionEvent(
                    BuiltinActionType.CONTINUE_CONVERSATION,
                    humanFriendlyMessage = label,
                    formState = formPayload(formName),
                    formName = formName,
                ),
            )
            return
        }
        for (step in ActionPlan.steps(ActionPlan.ownSteps(action!!))) {
            if (!runStep(step, formName)) return
        }
    }

    /** Runs one step of a plan; `false` when a mutation failed and the plan halts. */
    private suspend fun runStep(step: OpenUIValue, formName: String?): Boolean {
        when (step["type"].stringValue) {
            ActionStepType.RUN -> {
                val statementId = step["statementId"].stringValue ?: return true
                if (step["refType"] == OpenUIValue.Str("mutation")) {
                    val argsAST = parseResult
                        ?.mutationStatements
                        ?.firstOrNull { it.statementId == statementId }
                        ?.argsAST
                    val arguments = argsAST?.let { evaluate(it).objectValue } ?: OpenUIObject.Empty
                    return queryManager.fireMutation(statementId, arguments)
                }
                queryManager.invalidate(listOf(statementId))
            }
            ActionStepType.CONTINUE_CONVERSATION -> onAction?.invoke(
                ActionEvent(
                    type = BuiltinActionType.CONTINUE_CONVERSATION,
                    params = OpenUIObject.of("context" to step["context"]),
                    humanFriendlyMessage = step["message"]?.let(JS::toString).orEmpty(),
                    formState = formPayload(formName),
                    formName = formName,
                ),
            )
            ActionStepType.OPEN_URL -> onAction?.invoke(
                ActionEvent(
                    type = BuiltinActionType.OPEN_URL,
                    params = OpenUIObject.of("url" to (step["url"] ?: OpenUIValue.Str(""))),
                    humanFriendlyMessage = "",
                    formState = formPayload(formName),
                    formName = formName,
                ),
            )
            ActionStepType.CUSTOM_ACTION -> {
                val name = step["name"].stringValue ?: return true
                onAction?.invoke(
                    ActionEvent(
                        type = name,
                        params = step["params"].objectValue ?: OpenUIObject.Empty,
                        humanFriendlyMessage = "",
                        formState = formPayload(formName),
                        formName = formName,
                    ),
                )
            }
            ActionStepType.SET -> {
                val target = step["target"].stringValue ?: return true
                val valueAST = step["valueAST"].expressionValue ?: return true
                store[target] = evaluate(valueAST)
            }
            ActionStepType.RESET -> {
                val declarations = parseResult?.stateDeclarations ?: OpenUIObject.Empty
                for (target in step["targets"].arrayValue.orEmpty().mapNotNull { it.stringValue }) {
                    store.set(target, declarations[target] ?: OpenUIValue.Null, pristine = true)
                }
            }
        }
        return true
    }

    private companion object {
        /** Evaluation that keeps changing state settles within a few passes, or gives up. */
        const val MAX_RECOMPUTE_PASSES = 8

        /** Unwraps `{ value, componentType }` form entries. */
        fun unwrapFieldValue(value: OpenUIValue?): OpenUIValue? = value.objectValue?.get("value") ?: value
    }
}
