package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A property delegate to be used with listeners.
 *
 * The real listener stored in [currentValue] isn't exposed externally, it's only
 * accessible through the [wrapper].
 *
 * The [wrapper] is exposed by the getter, and a reference to it can be safely stored
 * long-term.
 *
 * Setting new listeners via the setter will update the underlying listener, and
 * calls to the [wrapper] will then be forwarded to the latest [currentValue] that
 * was set.
 *
 * @param wrap A function that has to produce the wrapper listener. The listener being
 *             wrapped can be referenced by calling the realListener() method. This
 *             function always returns the current listener, even if it changes.
 */
internal class Provider<L : Any>(
    initialValue: L,
    wrap: (provider: () -> L) -> L
) : ReadWriteProperty<Any?, L> {

    private var currentValue: L = initialValue
    private val wrapper: L = wrap { currentValue }

    override fun getValue(thisRef: Any?, property: KProperty<*>): L {
        return wrapper
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: L) {
        currentValue = value
    }
}
