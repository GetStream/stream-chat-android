package io.getstream.chat.android.offline.utils

/**
 * Used as a wrapper for data that represents an event.
 */
public open class Event<out T>(private val content: T) {

    @Suppress("MemberVisibilityCanBePrivate")
    public var hasBeenHandled: Boolean = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    public fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public fun peekContent(): T = content
}
