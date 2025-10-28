/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.test

/**
 * A collection that maps keys to values, similar to [Map], but in which each key may be
 * associated with multiple values.
 *
 * For testing purposes only.
 */
public class MultiMap<K, V>(
    input: List<Pair<K, V>>,
) : Map<K, V> {
    private val _entries: List<Map.Entry<K, V>> = input.map { Entry(it.first, it.second) }
    private val _keys: List<K> = _entries.map { it.key }
    private val _values: List<V> = _entries.map { it.value }

    override val keys: Set<K> = _keys.toSet()
    override val values: Collection<V> = _values.toSet()
    override val entries: Set<Map.Entry<K, V>> = _entries.toSet()
    override val size: Int = _entries.size

    override fun containsKey(key: K): Boolean = keys.contains(key)

    override fun containsValue(value: V): Boolean = values.contains(value)

    override fun get(key: K): V? = entries.find { it.key == key }?.value

    override fun isEmpty(): Boolean = entries.isEmpty()

    override fun toString(): String = "{${entries.joinToString { "${it.key}=${it.value}" }}}"

    private class Entry<K, V>(
        override val key: K,
        override val value: V,
    ) : Map.Entry<K, V>
}

/**
 * Returns an immutable [MultiMap], mapping only the specified key to the specified value.
 *
 * If multiple pairs have the same key, the resulting map will contain all values.
 */
public fun <K, V> multiMapOf(vararg input: Pair<K, V>): MultiMap<K, V> = MultiMap(input.toList())
