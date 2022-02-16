package io.getstream.chat.android.client.setup

import io.getstream.chat.android.client.models.User

/**
 * Coordinates the initialization of the Chat SDK
 */
internal class InitializationCoordinator private constructor() {

    private val userDisconnectedListeners: MutableList<(User?) -> Unit> = mutableListOf()
    private val userConnectedListeners: MutableList<(User) -> Unit> = mutableListOf()

    /**
     * Adds a listener to user connection
     */
    fun addUserConnectedListener(listener: (User) -> Unit) {
        userConnectedListeners.add(listener)
    }

    /**
     * Adds a listener to user disconnection
     */
    fun addUserDisconnectedListener(listener: (User?) -> Unit) {
        userDisconnectedListeners.add(listener)
    }

    /**
     * Notifies user connection
     */
    fun userConnected(user: User) {
        userConnectedListeners.forEach { function -> function.invoke(user) }
    }

    /**
     * Notifies user disconnection
     */
    fun userDisconnected(user: User) {
        userConnectedListeners.forEach { function -> function.invoke(user) }
    }

    companion object {
        private var instance: InitializationCoordinator? = null

        fun getOrCreate(): InitializationCoordinator =
            instance ?: InitializationCoordinator().also { instance = it }
    }
}
