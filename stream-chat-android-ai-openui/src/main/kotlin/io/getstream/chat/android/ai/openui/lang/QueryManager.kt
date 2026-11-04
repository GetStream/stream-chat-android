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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Reactive data fetching for `Query()` and `Mutation()` (port of `queryManager.ts`).
 *
 * Results are cached by tool name, arguments and dependency values; while a query refetches
 * with new arguments the previous data stays visible. Queries can auto-refresh on an interval.
 * A query whose arguments change waits [ARGUMENT_DEBOUNCE] before fetching, so arguments bound
 * to an input fetch once the user pauses rather than on every keystroke.
 *
 * Fetches run in [scope]. Like the runtime that owns it, the manager is confined to one thread:
 * give it a single-threaded scope (the main thread on Android).
 *
 * @param toolProvider Executes the tools; without one, queries show their defaults.
 */
internal class QueryManager(
    val toolProvider: ToolProvider?,
    private val scope: CoroutineScope,
) {
    /** An evaluated `Query()` statement. */
    data class QueryNode(
        val statementId: String,
        val toolName: String,
        val args: OpenUIValue,
        val defaults: OpenUIValue,
        /** Evaluated dependency values, part of the cache key so a change refetches. */
        val deps: OpenUIValue? = null,
        /** Auto-refresh interval in seconds. */
        val refreshInterval: Double? = null,
        val complete: Boolean = true,
    )

    /** An evaluated `Mutation()` statement. */
    data class MutationNode(val statementId: String, val toolName: String)

    /** Query results by statement id, plus loading and error state. */
    data class Snapshot(
        val results: OpenUIObject = OpenUIObject.Empty,
        val loading: List<String> = emptyList(),
        val refetching: List<String> = emptyList(),
        val errors: List<OpenUIError> = emptyList(),
    )

    private class QueryEntry(
        var toolName: String,
        var args: OpenUIValue,
        var defaults: OpenUIValue,
        var cacheKey: String,
    ) {
        var previousCacheKey: String? = null
        var loading = false
        var everFetched = false
        var refreshInterval = 0.0
        var timer: Job? = null

        /** A fetch waiting out the argument debounce. */
        var pendingFetch: Job? = null
        var needsRefetch = false
        var error: OpenUIError? = null
    }

    private class MutationEntry(var toolName: String, var result: OpenUIValue) {
        var error: OpenUIError? = null
    }

    /** `data == null` means no data yet (distinct from a `null` result). */
    private class CacheEntry(var data: OpenUIValue? = null, var inFlight: Boolean = false)

    private val queries = LinkedHashMap<String, QueryEntry>()
    private val mutations = LinkedHashMap<String, MutationEntry>()
    private val cache = HashMap<String, CacheEntry>()
    private val state = MutableStateFlow(Snapshot())
    private var snapshotJson = ""
    private var disposed = false
    private var generation = 0

    /** Results, loading and errors, rebuilt after every change. */
    val snapshot: StateFlow<Snapshot> = state.asStateFlow()

    /** Called after the snapshot changes. */
    internal var onChange: (() -> Unit)? = null

    // Snapshot ---------------------------------------------------------------------------------

    private fun rebuildSnapshot(): Boolean {
        val results = OpenUIObject.Builder()
        val loading = ArrayList<String>()
        val refetching = ArrayList<String>()
        val errors = ArrayList<OpenUIError>()
        for ((id, query) in queries) {
            results[id] = resultOf(query)
            if (query.loading) {
                loading += id
                if (query.everFetched) refetching += id
            }
            query.error?.let { errors += it }
        }
        for ((id, mutation) in mutations) {
            results[id] = mutation.result
            mutation.error?.let { errors += it }
        }
        val next = Snapshot(results.build(), loading, refetching, errors)
        val json = OpenUIJson.stringify(OpenUIValue.Obj(next.results)) + loading + refetching + errors
        if (json == snapshotJson) return false
        snapshotJson = json
        state.value = next
        return true
    }

    private fun notifyChange() {
        onChange?.invoke()
    }

    /** Settled data (even while refetching), else the previous key's data, else the defaults. */
    private fun resultOf(query: QueryEntry): OpenUIValue = cache[query.cacheKey]?.data
        ?: query.previousCacheKey?.let { cache[it]?.data }
        ?: query.defaults

    // Fetching ---------------------------------------------------------------------------------

    private fun executeFetch(cacheKey: String, statementId: String) {
        val provider = toolProvider ?: return
        val query = queries[statementId] ?: return
        val toolName = query.toolName
        val args = query.args

        val entry = cache.getOrPut(cacheKey) { CacheEntry() }
        entry.inFlight = true
        query.loading = true
        rebuildSnapshot()
        notifyChange()

        scope.launch {
            try {
                val data = provider.callTool(toolName, args.objectValue ?: OpenUIObject.Empty, userInitiated = false)
                if (disposed) return@launch
                // The data belongs to the key, so every query on it takes it. With none left (all
                // removed or moved to a different key while in flight) it is discarded.
                val sharing = queriesOn(cacheKey, entry)
                if (sharing.isEmpty()) {
                    entry.inFlight = false
                    cleanupCacheEntry(cacheKey)
                    return@launch
                }
                entry.data = data
                for (query in sharing.values) {
                    query.everFetched = true
                    query.error = null
                    // Clear the previous key first so cleanup doesn't see it as referenced.
                    query.previousCacheKey?.takeIf { it != cacheKey }?.let { previous ->
                        query.previousCacheKey = null
                        cleanupCacheEntry(previous)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (@Suppress("TooGenericExceptionCaught") error: Throwable) {
                // Any failure of the app's tool is reported to the model as a query error.
                for ((id, query) in queriesOn(cacheKey, entry)) {
                    query.error = toolError(OpenUIError.Source.Query, id, toolName, error)
                }
            } finally {
                entry.inFlight = false
                // Every query waiting on the key settles, not only the one that started the fetch.
                val waiting = queriesOn(cacheKey, entry)
                for (query in waiting.values) query.loading = false
                if (rebuildSnapshot()) notifyChange()
                // Refetch when invalidated while in flight.
                val invalidated = waiting.entries.firstOrNull { it.value.needsRefetch }?.key
                if (invalidated != null && !disposed) {
                    for (query in waiting.values) query.needsRefetch = false
                    executeFetch(cacheKey, invalidated)
                }
            }
        }
    }

    /** The queries on [cacheKey], while [entry] is still its cache entry. */
    private fun queriesOn(cacheKey: String, entry: CacheEntry): Map<String, QueryEntry> =
        if (cache[cacheKey] === entry) queries.filterValues { it.cacheKey == cacheKey } else emptyMap()

    private fun toolError(
        source: OpenUIError.Source,
        statementId: String,
        toolName: String,
        error: Throwable,
    ): OpenUIError {
        val kind = if (source == OpenUIError.Source.Query) "Query" else "Mutation"
        return when (error) {
            is ToolNotFoundException -> OpenUIError(
                source,
                "tool-not-found",
                "$kind tool \"$toolName\" not found",
                statementId,
                kind,
                toolName = toolName,
                hint = error.availableTools.takeIf { it.isNotEmpty() }?.let {
                    "Available tools: ${it.joinToString(
                        ", ",
                    )}"
                },
            )
            is McpToolException -> OpenUIError(
                source,
                "mcp-error",
                "$kind \"$toolName\" returned an error: ${error.toolErrorText}",
                statementId,
                kind,
                toolName = toolName,
            )
            else -> OpenUIError(
                source,
                "tool-error",
                "$kind \"$toolName\" failed: ${error.message ?: error.toString()}",
                statementId,
                kind,
                toolName = toolName,
            )
        }
    }

    /** Removes a cache entry no query references. */
    private fun cleanupCacheEntry(cacheKey: String) {
        if (queries.values.any { it.cacheKey == cacheKey || it.previousCacheKey == cacheKey }) return
        cache.remove(cacheKey)
    }

    // Queries ----------------------------------------------------------------------------------

    /** Syncs the active queries: fetches new or changed ones, drops removed ones, (re)arms timers. */
    fun evaluateQueries(nodes: List<QueryNode>) {
        if (disposed) return
        val activeIds = nodes.mapTo(HashSet()) { it.statementId }
        val removed = queries.filterKeys { it !in activeIds }
        for ((id, query) in removed) {
            query.timer?.cancel()
            query.pendingFetch?.cancel()
            queries.remove(id)
            cleanupCacheEntry(query.cacheKey)
            query.previousCacheKey?.let(::cleanupCacheEntry)
        }

        for (node in nodes) {
            if (!node.complete) continue
            val cacheKey = cacheKey(node.toolName, node.args, node.deps)
            val existing = queries[node.statementId]
            val argumentsChanged = existing != null && existing.cacheKey != cacheKey
            val query = if (existing != null) {
                // Keep the previous key for display while the new one loads.
                if (argumentsChanged) {
                    existing.previousCacheKey = existing.cacheKey
                    // A fetch scheduled for the old arguments is replaced.
                    existing.pendingFetch?.cancel()
                    existing.pendingFetch = null
                }
                existing.toolName = node.toolName
                existing.args = node.args
                existing.defaults = node.defaults
                existing.cacheKey = cacheKey
                existing
            } else {
                QueryEntry(node.toolName, node.args, node.defaults, cacheKey).also { queries[node.statementId] = it }
            }

            // Fetch when there is no settled data and nothing in flight.
            val entry = cache[cacheKey]
            val hasSettledData = entry?.data != null && !entry.inFlight
            if (toolProvider != null && !hasSettledData && entry?.inFlight != true) {
                when {
                    argumentsChanged -> scheduleFetch(query, node.statementId)
                    // Same arguments as a fetch already waiting: let it fire.
                    query.pendingFetch?.isActive == true -> Unit
                    else -> executeFetch(cacheKey, node.statementId)
                }
            } else if (hasSettledData && query.loading) {
                // Back on args whose data is cached while another fetch was in flight: that fetch
                // no longer concerns this query, so it must not keep it loading.
                query.loading = false
                query.previousCacheKey?.takeIf { it != cacheKey }?.let { previous ->
                    query.previousCacheKey = null
                    cleanupCacheEntry(previous)
                }
            }

            armRefresh(query, node)
        }
        if (rebuildSnapshot()) notifyChange()
    }

    /** (Re)arms the auto-refresh timer of [query] when its interval changed. */
    private fun armRefresh(query: QueryEntry, node: QueryNode) {
        val interval = node.refreshInterval ?: 0.0
        if (interval == query.refreshInterval) return
        query.timer?.cancel()
        query.timer = null
        if (interval > 0) {
            val wait = refreshDelay(interval, toolProvider?.minimumRefreshInterval ?: DEFAULT_MINIMUM_REFRESH)
            query.timer = scope.launch {
                while (isActive) {
                    delay(wait)
                    if (disposed || toolProvider == null) continue
                    if (cache[query.cacheKey]?.inFlight != true) executeFetch(query.cacheKey, node.statementId)
                }
            }
        }
        query.refreshInterval = interval
    }

    /**
     * Fetches [query]'s current key after [ARGUMENT_DEBOUNCE], unless its arguments change again
     * first. It shows as loading meanwhile, with the previous data still visible.
     */
    private fun scheduleFetch(query: QueryEntry, statementId: String) {
        query.loading = true
        query.pendingFetch = scope.launch {
            delay(ARGUMENT_DEBOUNCE)
            query.pendingFetch = null
            if (disposed || queries[statementId] !== query) return@launch
            val entry = cache[query.cacheKey]
            when {
                entry?.inFlight == true -> Unit
                entry?.data != null -> {
                    query.loading = false
                    if (rebuildSnapshot()) notifyChange()
                }
                else -> executeFetch(query.cacheKey, statementId)
            }
        }
    }

    /** The current result of a query: data, previous data while refetching, or its defaults. */
    fun result(statementId: String): OpenUIValue? = queries[statementId]?.let(::resultOf)

    fun isLoading(statementId: String): Boolean = queries[statementId]?.loading ?: false

    val isAnyLoading: Boolean get() = queries.values.any { it.loading }

    /** Refetches the given queries (all when empty); in-flight ones refetch when they settle. */
    fun invalidate(statementIds: List<String> = emptyList()) {
        if (disposed || toolProvider == null) return
        val targets = if (statementIds.isNotEmpty()) statementIds.filter { it in queries } else queries.keys.toList()
        for (id in targets) {
            val query = queries[id] ?: continue
            if (cache[query.cacheKey]?.inFlight == true) query.needsRefetch = true else executeFetch(query.cacheKey, id)
        }
    }

    // Mutations --------------------------------------------------------------------------------

    /** Syncs the declared mutations. */
    fun registerMutations(nodes: List<MutationNode>) {
        val activeIds = nodes.mapTo(HashSet()) { it.statementId }
        mutations.keys.retainAll(activeIds)
        for (node in nodes) {
            val existing = mutations[node.statementId]
            if (existing == null) {
                mutations[node.statementId] = MutationEntry(node.toolName, IDLE)
            } else if (existing.toolName != node.toolName) {
                existing.toolName = node.toolName
                existing.result = IDLE
                existing.error = null
            }
        }
        if (rebuildSnapshot()) notifyChange()
    }

    /**
     * Runs a mutation with already evaluated arguments. Returns whether it succeeded; a call
     * while the same mutation is running is rejected. On success, [refreshQueryIds] refetch.
     */
    suspend fun fireMutation(
        statementId: String,
        arguments: OpenUIObject,
        refreshQueryIds: List<String> = emptyList(),
    ): Boolean {
        val provider = toolProvider
        if (disposed || provider == null) return false
        val mutation = mutations[statementId] ?: return false
        if (mutation.result["status"] == OpenUIValue.Str("loading")) return false

        val startGeneration = generation
        val loading = status("loading")
        mutation.result = loading
        rebuildSnapshot()
        notifyChange()

        var success = false
        try {
            val data = provider.callTool(mutation.toolName, arguments, userInitiated = true)
            if (disposed || startGeneration != generation) return false
            mutation.result = OpenUIValue.obj("status" to OpenUIValue.Str("success"), "data" to data)
            mutation.error = null
            success = true
        } catch (error: CancellationException) {
            throw error
        } catch (@Suppress("TooGenericExceptionCaught") error: Throwable) {
            // Any failure of the app's tool is reported to the model as a mutation error.
            if (disposed || startGeneration != generation) return false
            mutation.result =
                OpenUIValue.obj(
                    "status" to OpenUIValue.Str("error"),
                    "error" to OpenUIValue.Str(error.message ?: error.toString()),
                )
            mutation.error = toolError(OpenUIError.Source.Mutation, statementId, mutation.toolName, error)
        } finally {
            // A cancelled call (its caller left the screen) never settles: back to idle so the
            // mutation can run again.
            if (mutation.result === loading) {
                mutation.result = IDLE
                if (rebuildSnapshot()) notifyChange()
            }
        }
        rebuildSnapshot()
        notifyChange()
        if (success && refreshQueryIds.isNotEmpty()) invalidate(refreshQueryIds)
        return success
    }

    /** The `{ status, data?, error? }` state of a mutation. */
    fun mutationResult(statementId: String): OpenUIValue? = mutations[statementId]?.result

    private fun status(value: String) = OpenUIValue.obj("status" to OpenUIValue.Str(value))

    // Lifecycle --------------------------------------------------------------------------------

    /** Resumes after [dispose]. */
    fun activate() {
        disposed = false
    }

    /** Stops timers and pending work; cached results are kept for re-activation. */
    fun dispose() {
        disposed = true
        generation++
        for (query in queries.values) {
            query.timer?.cancel()
            query.timer = null
            query.pendingFetch?.cancel()
            query.pendingFetch = null
            query.refreshInterval = 0.0
            query.loading = false
            query.needsRefetch = false
        }
        mutations.clear()
    }

    internal companion object {
        private val DEFAULT_MINIMUM_REFRESH = 5.seconds

        /** The state of a mutation that hasn't run, or whose last run was cancelled. */
        private val IDLE = OpenUIValue.obj(
            "status" to OpenUIValue.Str("idle"),
            "data" to OpenUIValue.Null,
            "error" to OpenUIValue.Null,
        )

        /** How long a query waits after its arguments change before fetching. */
        internal val ARGUMENT_DEBOUNCE = 300.milliseconds
        private val SHORTEST = 1.milliseconds

        // A browser's `setInterval` waits at most 2^31 - 1 ms (about 24.8 days).
        private val LONGEST = Int.MAX_VALUE.toLong().milliseconds

        /**
         * The model writes the interval, so it can be any number of seconds: it waits at least the
         * tool provider's [minimum], and like a browser's `setInterval`, at least a millisecond and
         * at most about 24.8 days.
         */
        fun refreshDelay(interval: Double, minimum: Duration): Duration {
            val shortest = minimum.coerceIn(SHORTEST, LONGEST)
            if (interval.isNaN()) return shortest
            if (interval >= LONGEST.inWholeSeconds) return LONGEST
            return interval.seconds.coerceIn(shortest, LONGEST)
        }

        /** JSON with sorted keys at every level; non-finite numbers become stable strings. */
        fun stableStringify(value: OpenUIValue): String = OpenUIJson.stringify(stable(value))

        private fun stable(value: OpenUIValue): OpenUIValue = when (value) {
            is OpenUIValue.Obj -> OpenUIValue.Obj(
                OpenUIObject(value.fields.toSortedMap().mapValues { stable(it.value) }),
            )
            is OpenUIValue.Arr -> OpenUIValue.Arr(value.items.map(::stable))
            is OpenUIValue.Num -> when {
                value.value.isNaN() -> OpenUIValue.Str("__NaN__")
                value.value == Double.POSITIVE_INFINITY -> OpenUIValue.Str("__Inf__")
                value.value == Double.NEGATIVE_INFINITY -> OpenUIValue.Str("__-Inf__")
                else -> value
            }
            is OpenUIValue.Element, is OpenUIValue.Expression -> stable(value.plainJson)
            else -> value
        }

        fun cacheKey(toolName: String, args: OpenUIValue, deps: OpenUIValue?): String {
            val depsKey = if (deps != null && deps !is OpenUIValue.Null) "::" + stableStringify(deps) else ""
            return toolName + "::" + stableStringify(args) + depsKey
        }
    }
}
