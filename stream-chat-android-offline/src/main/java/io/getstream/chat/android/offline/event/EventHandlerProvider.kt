package io.getstream.chat.android.offline.event

internal object EventHandlerProvider {

    private var _eventHandler: EventHandlerImpl? = null

    internal var eventHandler: EventHandlerImpl
        get() = _eventHandler ?: throw IllegalStateException("EventHandlerImpl was not set in the EventHandlerProvider. Looks like there's a initialisation problem")
        set(value) {
            _eventHandler = value
        }
}
