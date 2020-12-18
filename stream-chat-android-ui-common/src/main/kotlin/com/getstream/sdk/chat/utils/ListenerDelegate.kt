package com.getstream.sdk.chat.utils

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
@InternalStreamChatApi
public class ListenerDelegate<L : Any>(
    initialValue: L,
    wrap: (realListener: () -> L) -> L
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
