package io.getstream.chat.android.client.setup

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.User

/**
 * Coordinates the initialization of the Chat SDK
 */
// Make it internal when ChatDomain merge is complete
public class InitializationCoordinator private constructor() {

    private val userDisconnectedListeners: MutableList<(User?) -> Unit> = mutableListOf()
    private val userConnectedListeners: MutableList<(User) -> Unit> = mutableListOf()
    private val userSetListeners: MutableList<(User) -> Unit> = mutableListOf()

    /**
     * Adds a listener to user set.
     */
    public fun addUserSetListener(listener: (User) -> Unit) {
        userSetListeners.add(listener)
    }

    /**
     * Adds a listener to user connection.
     */
    public fun addUserConnectedListener(listener: (User) -> Unit) {
        userConnectedListeners.add(listener)
    }

    /**
     * Adds a listener to user disconnection.
     */
    public fun addUserDisconnectedListener(listener: (User?) -> Unit) {
        userDisconnectedListeners.add(listener)
    }

    /**
     * Notifies user set
     */
    internal fun userSet(user: User) {
        userSetListeners.forEach { function -> function.invoke(user) }
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

        /**
         * Gets the initialization coordinator or creates it if necessary.
         */
        public fun getOrCreate(): InitializationCoordinator =
            instance ?: create().also { instance = it }

        @VisibleForTesting
        internal fun create(): InitializationCoordinator = InitializationCoordinator()
    }
}
