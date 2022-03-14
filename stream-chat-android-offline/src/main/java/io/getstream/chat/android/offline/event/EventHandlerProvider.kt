package io.getstream.chat.android.offline.event

/**
 * Provider of EventHandlerImpl. This should be used is services and Workers.
 */
internal object EventHandlerProvider {

    private var _eventHandler: EventHandlerImpl? = null

    /**
     * The [EventHandlerImpl]
     */
    internal var eventHandler: EventHandlerImpl
        get() = _eventHandler ?: throw IllegalStateException("EventHandlerImpl was not set in the EventHandlerProvider. Looks like there's a initialisation problem")
        set(value) {
            _eventHandler = value
        }
}
