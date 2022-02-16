package io.getstream.chat.android.client.setup

import io.getstream.chat.android.client.models.User

// Make it internal once ChatDomain is merged with ChatClient.
public class InitializationCoordinator private constructor() {

    private val userDisconnectedListeners: MutableList<(User?) -> Unit> = mutableListOf()
    private val userConnectedListeners: MutableList<(User) -> Unit> = mutableListOf()

    public fun addUserConnectedListener(listener: (User) -> Unit) {
        userConnectedListeners.add(listener)
    }

    public fun addUserDisconnectedListener(listener: (User?) -> Unit) {
        userDisconnectedListeners.add(listener)
    }

    public fun userConnected(user: User) {
        userConnectedListeners.forEach { function -> function.invoke(user) }
    }

    public fun userDisconnected(user: User) {
        userConnectedListeners.forEach { function -> function.invoke(user) }
    }

    public companion object {
        private var instance: InitializationCoordinator? = null

        public fun getOrCreate(): InitializationCoordinator =
            instance ?: InitializationCoordinator().also { instance = it }
    }
}
