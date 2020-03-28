package io.getstream.chat.android.client.api.models


abstract class BaseQueryChannelRequest<T : BaseQueryChannelRequest<T>> {

    var state = false
    var watch = false
    var presence = false

    protected abstract fun cloneOpts(): T

    fun withWatch(): T {
        val clone = cloneOpts()
        (clone as BaseQueryChannelRequest<*>).watch = true
        return clone
    }

    fun noWatch(): T {
        val clone = cloneOpts()
        (clone as BaseQueryChannelRequest<*>).watch = false
        return clone
    }

    fun withState(): T {
        val clone = cloneOpts()
        (clone as BaseQueryChannelRequest<*>).state = true
        return clone
    }

    fun noState(): T {
        val clone = cloneOpts()
        (clone as BaseQueryChannelRequest<*>).state = false
        return clone
    }

    open fun withPresence(): T {
        val clone = cloneOpts()
        (clone as BaseQueryChannelRequest<*>).presence = true
        return clone
    }

    fun noPresence(): T {
        val clone = cloneOpts()
        (clone as BaseQueryChannelRequest<*>).presence = false
        return clone
    }
}
