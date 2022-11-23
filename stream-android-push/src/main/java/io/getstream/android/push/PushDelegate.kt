package io.getstream.android.push

public interface PushDelegate {

    /**
     * Handles push message payload.
     * If the payload can't be handled because doesn't contain the needed data, return false to notify you this
     * push message payload needs to be handled by you.
     *
     * @param payload The payload to be handled.
     * @return True if the payload was handled.
     */
    public fun handlePushMessage(payload: Map<String, Any?>): Boolean

    /**
     * Register a new [PushDevice]
     *
     * @param pushDevice The PushDevice to be registered.
     */
    public fun registerPushDevice(pushDevice: PushDevice)
}