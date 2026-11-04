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
 * What each referenced statement materialized to while a response streams, with everything that
 * materialization read and reported, so the next pass reuses it as long as none of the statements
 * it read has changed.
 *
 * A result is kept only when materializing it met no cycle and no depth or node limit, since
 * those depend on where the statement is used; it is reused only where it fits the depth and
 * nodes left, so a reused subtree counts against [Materializer.MAX_NODES] like a fresh one.
 */
internal class MaterializationCache {
    data class Key(val name: String, val expression: Boolean, val partial: Boolean)

    /** A statement read by name, with the version it had, or `null` when it wasn't declared. */
    class Read(val name: String, val version: Int?)

    @Suppress("LongParameterList")
    class Entry(
        val result: OpenUIValue,
        val version: Int?,
        val reads: List<Read>,
        val errors: List<ValidationError>,
        val unresolved: List<String>,
        val reached: List<String>,
        val nodes: Int,
        val height: Int,
    )

    /** The `$variables` a statement uses, for the statement's [version]. */
    class StateReferences(val version: Int, val names: List<String>)

    /** Each statement's version, which changes whenever its expression may have. */
    var versions: Map<String, Int> = emptyMap()
    val entries = HashMap<Key, Entry>()
    val stateReferences = HashMap<String, StateReferences>()

    /** How many nodes were materialized afresh, not reused, over all passes. */
    var materializedNodes = 0

    fun isCurrent(reads: List<Read>): Boolean = reads.all { versions[it.name] == it.version }
}

/** Where a statement's materialization started in the logs of its pass. */
@Suppress("LongParameterList")
internal class MaterializationStart(
    val reads: Int,
    val errors: Int,
    val unresolved: Int,
    val reached: Int,
    val nodes: Int,
    val cycles: Int,
    val truncations: Int,
    val depth: Int,
    val deepest: Int,
)

/** Looks up statement [name], recording the read when a cache is kept. */
internal fun MaterializeContext.lookUp(name: String): ASTNode? {
    val cache = cache
    if (cache != null) reads += MaterializationCache.Read(name, cache.versions[name])
    return symbols[name]
}

/**
 * A statement's earlier result, reporting again what it reported, when everything it read is
 * unchanged and it fits the depth and nodes left.
 */
internal fun MaterializeContext.reuse(name: String, expression: Boolean): OpenUIValue? {
    val cache = cache ?: return null
    val entry = cache.entries[MaterializationCache.Key(name, expression, partial)] ?: return null
    if (entry.version != cache.versions[name] || !cache.isCurrent(entry.reads)) return null
    // A subtree reused where it doesn't fit would bypass the limits a fresh one meets.
    if (depth + entry.height > Materializer.MAX_DEPTH || nodes + entry.nodes > Materializer.MAX_NODES) return null
    reads += entry.reads
    errors += entry.errors
    unresolved += entry.unresolved
    for (id in entry.reached) markReached(id)
    nodes += entry.nodes
    deepest = maxOf(deepest, depth + entry.height)
    return entry.result
}

internal fun MaterializeContext.startMaterializing(): MaterializationStart? {
    if (cache == null) return null
    val start = MaterializationStart(
        reads = reads.size,
        errors = errors.size,
        unresolved = unresolved.size,
        reached = reachedLog.size,
        nodes = nodes,
        cycles = cycles,
        truncations = truncations,
        depth = depth,
        deepest = deepest,
    )
    deepest = depth
    return start
}

/** Keeps a statement's result, unless it met a cycle or a limit, which depend on where it is used. */
internal fun MaterializeContext.finishMaterializing(
    start: MaterializationStart?,
    name: String,
    expression: Boolean,
    result: OpenUIValue,
) {
    val cache = cache
    if (start == null || cache == null) return
    val height = deepest - start.depth
    deepest = maxOf(start.deepest, deepest)
    if (cycles != start.cycles || truncations != start.truncations) return
    val readNames = HashSet<String>()
    val reachedNames = HashSet<String>()
    cache.entries[MaterializationCache.Key(name, expression, partial)] = MaterializationCache.Entry(
        result = result,
        version = cache.versions[name],
        reads = reads.subList(start.reads, reads.size).filter { readNames.add(it.name) },
        errors = errors.subList(start.errors, errors.size).toList(),
        unresolved = unresolved.subList(start.unresolved, unresolved.size).toList(),
        reached = reachedLog.subList(start.reached, reachedLog.size).filter { reachedNames.add(it) },
        nodes = nodes - start.nodes,
        height = height,
    )
}
