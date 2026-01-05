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

package io.getstream.chat.android.ui.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Creates a new instance of [LazyVarDelegate] that uses the specified initialization
 * function for the default value.
 *
 * @param initializer The initializer for the default value.
 * @return [LazyVarDelegate] instance that holds a value or a value override.
 */
internal fun <T : Any> lazyVar(initializer: () -> T): ReadWriteProperty<Any?, T> {
    return LazyVarDelegate(initializer)
}

/**
 * A property delegate that represents a value with lazy initialization. It is also
 * possible to set the value externally. If the property was not accessed before the
 * external value was set, then the default value will not initialized.
 *
 * @param initializer The initializer for the default value.
 */
internal class LazyVarDelegate<T : Any>(
    initializer: () -> T,
) : ReadWriteProperty<Any?, T> {
    private val defaultValue: T by lazy(initializer)
    private var overrideValue: T? = null

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        overrideValue = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return overrideValue ?: defaultValue
    }
}
