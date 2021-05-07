package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.offline.utils.Event as OfflineEvent

/**
 * Used as a wrapper for data that represents an event.
 */
public open class Event<out T> internal constructor(private val offlineEvent: OfflineEvent<T>) {

    public constructor(content: T) : this(OfflineEvent(content))

    @Suppress("MemberVisibilityCanBePrivate")
    public val hasBeenHandled: Boolean
        get() = offlineEvent.hasBeenHandled

    /**
     * Returns the content and prevents its use again.
     */
    public fun getContentIfNotHandled(): T? = offlineEvent.getContentIfNotHandled()

    /**
     * Returns the content, even if it's already been handled.
     */
    public fun peekContent(): T = offlineEvent.peekContent()
}
