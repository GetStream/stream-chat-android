package io.getstream.chat.android.client.setup

import io.getstream.chat.android.client.models.User

/**
 * Coordinates the initialization of the Chat SDK
 */
// Make it internal when ChatDomain merge is complete
public class InitializationCoordinator private constructor() {

    private val userDisconnectedListeners: MutableList<(User?) -> Unit> = mutableListOf()
    private val userConnectedListeners: MutableList<(User) -> Unit> = mutableListOf()

    /**
     * Adds a listener to user connection
     */
    internal fun addUserConnectedListener(listener: (User) -> Unit) {
        userConnectedListeners.add(listener)
    }

    /**
     * Adds a listener to user disconnection
     */
    public fun addUserDisconnectedListener(listener: (User?) -> Unit) {
        userDisconnectedListeners.add(listener)
    }

    /**
     * Notifies user connection
     */
    internal fun userConnected(user: User) {
        userConnectedListeners.forEach { function -> function.invoke(user) }
    }

    /**
     * Notifies user disconnection
     */
    internal fun userDisconnected(user: User?) {
        userDisconnectedListeners.forEach { function -> function.invoke(user) }
    }

    public companion object {
        private var instance: InitializationCoordinator? = null

        public fun getOrCreate(): InitializationCoordinator =
            instance ?: InitializationCoordinator().also { instance = it }
    }
}
