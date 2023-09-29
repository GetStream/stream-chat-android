package io.getstream.chat.android.offline.event.handler.internal.model

internal class MutableValue<T>(initialValue: T) {

    private var modified = false
    private var currentValue: T = initialValue

    fun isModified(): Boolean = modified

    fun get(): T = currentValue

    fun modify(block: (T) -> T) {
        set(block(currentValue))
    }

    fun set(value: T) {
        if (currentValue != value) {
            currentValue = value
            modified = true
        }
    }

}

internal fun <T> MutableValue<T>.useIfModified(block: (T) -> Unit) {
    if (isModified()) {
        block(get())
    }
}

internal fun <T> MutableValue<T?>.useNotNullIfModified(block: (T) -> Unit) {
    if (isModified()) {
        get()?.also(block)
    }
}