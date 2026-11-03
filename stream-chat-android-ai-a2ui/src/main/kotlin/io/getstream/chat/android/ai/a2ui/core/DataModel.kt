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

/**
 * A JSON Pointer ([RFC 6901](https://www.rfc-editor.org/rfc/rfc6901)) into a surface's data
 * model. A path that starts with `/` is absolute; any other path is relative to the scope it
 * is read in, such as the item of a list template.
 */
internal data class DataPath(val segments: List<String>, val isAbsolute: Boolean) {

    /** Whether it addresses the whole data model. */
    val isRoot: Boolean get() = isAbsolute && segments.isEmpty()

    /** [other] read in this path's scope: an absolute [other] as it is. */
    fun appending(other: DataPath): DataPath {
        if (other.isAbsolute) return other
        // `.` is the scope itself, so `.` and `./name` resolve inside it.
        return DataPath(segments + other.segments.filter { it != "." }, isAbsolute)
    }

    /** The path one level down, at [segment]. */
    fun appending(segment: String): DataPath = DataPath(segments + segment, isAbsolute)

    /** Whether this path is [other] or inside it. */
    fun isDescendant(other: DataPath): Boolean =
        other.segments.size <= segments.size && other.segments.indices.all { segments[it] == other.segments[it] }

    override fun toString(): String {
        val path = segments.joinToString("/") { it.replace("~", "~0").replace("/", "~1") }
        return if (isAbsolute) "/$path" else path
    }

    companion object {
        /** The whole data model. */
        val Root: DataPath = DataPath(emptyList(), isAbsolute = true)

        /** Parses a pointer. Empty segments, as in `//a//`, are skipped. */
        fun parse(path: String): DataPath = DataPath(
            segments = path.split('/').filter { it.isNotEmpty() }.map { it.replace("~1", "/").replace("~0", "~") },
            isAbsolute = path.startsWith("/"),
        )

        /** Whether every `~` of [path] starts a `~0` or `~1` escape. */
        fun isValidSyntax(path: String): Boolean {
            var index = 0
            while (index < path.length) {
                if (path[index] == '~') {
                    val next = path.getOrNull(index + 1)
                    if (next != '0' && next != '1') return false
                    index++
                }
                index++
            }
            return true
        }
    }
}

/** A data model write that can't be applied. The data model is left as it was. */
internal class DataModelException(message: String) : Exception(message)

/**
 * The data of one surface: a JSON value, normally an object, read and written by [DataPath].
 *
 * The agent replaces or patches it with `updateDataModel`, and inputs write to it as the person
 * types. Writing `null` removes the entry; removing a list item leaves `null` in its place so
 * later indices keep their meaning. Observers hear about a path when its value changes.
 *
 * Not thread-safe: the renderer uses it from the main thread.
 */
internal class DataModel(initial: Any? = emptyMap<String, Any?>()) {

    private var storage: Any? = A2UIJson.normalize(initial)
    private val observers = mutableListOf<Observer>()
    private var disposed = false

    /** The whole data model. */
    val snapshot: Any? get() = storage

    /** The value at [path], or `null` when there is none. */
    fun get(path: DataPath): Any? {
        checkSegments(path)
        return read(storage, path.segments)
    }

    /**
     * Writes [value] at [path], creating the objects and lists on the way. `null` removes the
     * entry. Writing at the root replaces the whole data model.
     *
     * @throws DataModelException When the path goes through a value that isn't an object or a
     * list, or names a list item that can't be.
     */
    fun set(path: DataPath, value: Any?) {
        checkSegments(path)
        val newValue = A2UIJson.normalize(value)
        storage = if (path.segments.isEmpty()) {
            newValue ?: emptyMap<String, Any?>()
        } else {
            if (newValue == null && read(storage, path.segments) == null) return
            write(storage, path.segments, 0, newValue, path)
        }
        notifyChange(path)
    }

    /**
     * Calls [listener] with the new value at [path] whenever it changes.
     *
     * @return Stops observing.
     */
    fun observe(path: DataPath, listener: (Any?) -> Unit): () -> Unit {
        val observer = Observer(path, read(storage, path.segments), listener)
        observers.add(observer)
        return { observers.remove(observer) }
    }

    /** Stops every observer. */
    fun dispose() {
        disposed = true
        observers.clear()
    }

    private fun notifyChange(path: DataPath) {
        if (disposed) return
        for (observer in observers.toList()) {
            val related = observer.path.isDescendant(path) || path.isDescendant(observer.path)
            if (!related) continue
            val value = read(storage, observer.path.segments)
            if (!A2UIJson.isEqual(observer.lastValue, value)) {
                observer.lastValue = value
                observer.listener(value)
            }
        }
    }

    private class Observer(val path: DataPath, var lastValue: Any?, val listener: (Any?) -> Unit)

    private fun write(current: Any?, segments: List<String>, index: Int, value: Any?, path: DataPath): Any? {
        if (index == segments.size) return value
        val segment = segments[index]
        val isLast = index == segments.size - 1
        return when (current) {
            is Map<*, *> -> {
                val map = LinkedHashMap<String, Any?>()
                current.forEach { (key, item) -> map[key as String] = item }
                if (isLast && value == null) {
                    map.remove(segment)
                } else {
                    val child = map[segment] ?: emptyContainer(segments.getOrNull(index + 1))
                    map[segment] = write(child, segments, index + 1, value, path)
                }
                map
            }
            is List<*> -> {
                val position = when {
                    segment == "-" -> current.size
                    else -> arrayIndex(segment)
                }
                if (position == null || position > MAX_ARRAY_INDEX) {
                    throw DataModelException(
                        "Cannot set path '$path': '$segment' is a non-numeric segment or out of bounds for an array.",
                    )
                }
                val list = current.toMutableList()
                if (isLast && value == null) {
                    if (position < list.size) list[position] = null
                    return list
                }
                val existing = list.getOrNull(position)
                val child = existing ?: emptyContainer(segments.getOrNull(index + 1))
                val written = write(child, segments, index + 1, value, path)
                // Writing past the end pads the gap with nulls.
                while (list.size < position) list.add(null)
                if (position < list.size) list[position] = written else list.add(written)
                list
            }
            else -> throw DataModelException(
                "Cannot set path '$path' because it descends through a value that is not an object or an array.",
            )
        }
    }

    private fun checkSegments(path: DataPath) {
        path.segments.firstOrNull { it in FORBIDDEN_SEGMENTS }?.let {
            throw DataModelException("Forbidden path segment '$it' in '$path'.")
        }
    }

    companion object {
        /** The largest list index a write may use, so one write can't allocate a huge list. */
        const val MAX_ARRAY_INDEX: Int = 10_000

        // Segments that would reach an object's prototype in a JavaScript renderer; rejected
        // everywhere so a payload means the same on every platform.
        private val FORBIDDEN_SEGMENTS = setOf("__proto__", "constructor", "prototype")

        /** The value at [segments] inside [value], or `null` when there is none. */
        fun read(value: Any?, segments: List<String>): Any? {
            var current = value
            for (segment in segments) {
                current = when (current) {
                    is Map<*, *> -> current[segment]
                    is List<*> -> arrayIndex(segment)?.let { current.getOrNull(it) }
                    else -> null
                } ?: return null
            }
            return current
        }

        /** An RFC 6901 list index: digits without a leading zero. */
        fun arrayIndex(segment: String): Int? {
            if (segment.isEmpty() || segment.any { it !in '0'..'9' }) return null
            if (segment.length > 1 && segment[0] == '0') return null
            return segment.toIntOrNull()
        }

        private fun emptyContainer(next: String?): Any? = when {
            next == null -> null
            next == "-" || arrayIndex(next) != null -> emptyList<Any?>()
            else -> emptyMap<String, Any?>()
        }
    }
}
