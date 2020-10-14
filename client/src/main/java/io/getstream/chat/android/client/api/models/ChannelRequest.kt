package io.getstream.chat.android.client.api.models

@Suppress("UNCHECKED_CAST")
internal interface ChannelRequest<T : ChannelRequest<T>> {

    var state: Boolean
    var watch: Boolean
    var presence: Boolean

    fun withWatch(): T {
        watch = true
        return this as T
    }

    fun withState(): T {
        state = true
        return this as T
    }

    fun noWatch(): T {
        watch = false
        return this as T
    }

    fun noState(): T {
        state = false
        return this as T
    }

    fun withPresence(): T {
        presence = true
        return this as T
    }

    fun noPresence(): T {
        presence = false
        return this as T
    }
}
