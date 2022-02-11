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
