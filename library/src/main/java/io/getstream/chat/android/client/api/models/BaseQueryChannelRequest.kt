package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName


abstract class BaseQueryChannelRequest<T : BaseQueryChannelRequest<T>> {

    @SerializedName("state")
    protected var state = false
    @SerializedName("watch")
    var watch = false
    @SerializedName("presence")
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
