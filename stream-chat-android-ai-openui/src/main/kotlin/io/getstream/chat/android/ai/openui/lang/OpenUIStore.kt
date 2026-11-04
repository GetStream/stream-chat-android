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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reactive state for a rendered program (port of `store.ts`).
 *
 * Holds `$variables` as top-level `"$name"` keys and form fields nested under their form name
 * (`{ formName: { field: { value, componentType } } }`). UIs observe [snapshot].
 *
 * Not thread-safe: use it from one thread (the UI thread), like the runtime that owns it.
 */
internal class OpenUIStore {
    private val state = MutableStateFlow(OpenUIObject.Empty)

    /** All state in insertion order. */
    val snapshot: StateFlow<OpenUIObject> = state.asStateFlow()

    /** Called after every change with the changed key. */
    internal var onChange: ((String) -> Unit)? = null

    /** Keys that still hold the default from their declaration (not set by the user or restored). */
    private val defaultedKeys = HashSet<String>()

    /** The value of [name] (a `"$name"` variable or a form name), or `null` when unset. */
    operator fun get(name: String): OpenUIValue? = state.value[name]

    /** Sets a value (`null` removes it). Equal values don't notify. */
    operator fun set(name: String, value: OpenUIValue?) {
        set(name, value, pristine = false)
    }

    /**
     * Sets a value (`null` removes it). Equal values don't notify. A [pristine] value, such as one
     * `@Reset` restored, still follows its declaration.
     */
    fun set(name: String, value: OpenUIValue?, pristine: Boolean) {
        if (pristine) defaultedKeys += name else defaultedKeys -= name
        if (state.value[name] == value) return
        state.value = state.value.with(name, value)
        onChange?.invoke(name)
    }

    /**
     * Applies persisted values (explicit restore) and declaration defaults. Values that were set
     * or restored are never overwritten or deleted, since declarations can temporarily disappear
     * while streaming. A value that still holds its declared default follows the declaration while
     * it streams in.
     */
    fun initialize(defaults: OpenUIObject, persisted: OpenUIObject) {
        val current = state.value
        val next = OpenUIObject.Builder().also { builder -> current.forEach { (key, value) -> builder[key] = value } }
        for ((key, value) in persisted) {
            next[key] = value
            defaultedKeys -= key
        }
        for ((key, value) in defaults) {
            if (key !in next || key in defaultedKeys) {
                next[key] = value
                defaultedKeys += key
            }
        }
        val built = next.build()
        val changedKeys = built.keys.filter { current[it] != built[it] }
        state.value = built
        changedKeys.forEach { onChange?.invoke(it) }
    }

    /** Clears all state. */
    fun reset() {
        state.value = OpenUIObject.Empty
        defaultedKeys.clear()
    }
}
