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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A property delegate to be used with listeners.
 *
 * The real listener stored in [realListener] isn't exposed externally, it's only
 * accessible through the [wrapper].
 *
 * The [wrapper] is exposed by the getter, and a reference to it can be safely stored
 * long-term.
 *
 * Setting new listeners via the setter will update the underlying listener, and
 * calls to the [wrapper] will then be forwarded to the latest [realListener] that
 * was set.
 *
 * @param wrap A function that has to produce the wrapper listener. The listener being
 *             wrapped can be referenced by calling the realListener() method. This
 *             function always returns the current listener, even if it changes.
 */
// TODO Needs to be renamed to something like WrapDelegate. It is no longer used for listeners only.
@InternalStreamChatApi
public class ListenerDelegate<L : Any>(
    initialValue: L,
    wrap: (realListener: () -> L) -> L,
) : ReadWriteProperty<Any?, L> {

    private var realListener: L = initialValue
    private val wrapper: L = wrap { realListener }

    override fun getValue(thisRef: Any?, property: KProperty<*>): L {
        return wrapper
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: L) {
        realListener = value
    }
}
