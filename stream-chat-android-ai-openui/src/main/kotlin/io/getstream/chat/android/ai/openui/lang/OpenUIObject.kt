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

import java.util.Collections

/**
 * An insertion-ordered, immutable string-keyed map, mirroring JavaScript object semantics.
 *
 * Key order is significant in OpenUI: positional component arguments are mapped onto props in
 * the order the library schema declares them, and objects are serialized in insertion order.
 * Re-assigning an existing key keeps its original position; removing a key drops it.
 * Equality is order-insensitive, like comparing two JavaScript objects' own properties.
 */
public class OpenUIObject private constructor(private val storage: LinkedHashMap<String, OpenUIValue>) :
    // An unmodifiable view, so a cast can't write through `entries` or `values`.
    Map<String, OpenUIValue> by Collections.unmodifiableMap(storage) {

    /** Creates an object from a map, keeping the map's iteration order. */
    public constructor(map: Map<String, OpenUIValue> = emptyMap()) : this(LinkedHashMap(map))

    /** A copy with `key` set to `value` (`null` removes it, like `delete obj[key]`). */
    public fun with(key: String, value: OpenUIValue?): OpenUIObject {
        val copy = LinkedHashMap(storage)
        if (value == null) copy.remove(key) else copy[key] = value
        return OpenUIObject(copy)
    }

    /** A copy without `key`. */
    public fun without(key: String): OpenUIObject = with(key, null)

    /** A copy with `other`'s entries assigned on top (like `{ ...this, ...other }`). */
    public operator fun plus(other: Map<String, OpenUIValue>): OpenUIObject {
        val copy = LinkedHashMap(storage)
        copy.putAll(other)
        return OpenUIObject(copy)
    }

    /** A copy with every value transformed, keeping key order. */
    public fun mapValues(transform: (OpenUIValue) -> OpenUIValue): OpenUIObject {
        val copy = LinkedHashMap<String, OpenUIValue>(storage.size)
        for ((key, value) in storage) copy[key] = transform(value)
        return OpenUIObject(copy)
    }

    override fun equals(other: Any?): Boolean = other is Map<*, *> && storage == other
    override fun hashCode(): Int = storage.hashCode()
    override fun toString(): String = JS.jsonStringify(OpenUIValue.Obj(this))!!

    /** Mutable builder that keeps JavaScript key-order semantics. */
    public class Builder internal constructor() {
        private val storage = LinkedHashMap<String, OpenUIValue>()

        /** The value for [key], or `null` when absent. */
        public operator fun get(key: String): OpenUIValue? = storage[key]

        /** Whether [key] is set. */
        public operator fun contains(key: String): Boolean = key in storage

        /** Sets a value (`null` removes the key). */
        public operator fun set(key: String, value: OpenUIValue?) {
            if (value == null) storage.remove(key) else storage[key] = value
        }

        /** An immutable copy of the entries set so far. */
        public fun build(): OpenUIObject = OpenUIObject(LinkedHashMap(storage))
    }

    /** Factories for objects. */
    public companion object {
        /** The empty object (`{}`). */
        public val Empty: OpenUIObject = OpenUIObject()

        /** Builds an object from pairs; `null` values are skipped (JavaScript `undefined`). */
        public fun of(vararg pairs: Pair<String, OpenUIValue?>): OpenUIObject = build {
            for ((key, value) in pairs) if (value != null) this[key] = value
        }

        /** Builds an object with a [Builder]. */
        public fun build(block: Builder.() -> Unit): OpenUIObject = Builder().apply(block).build()
    }
}
