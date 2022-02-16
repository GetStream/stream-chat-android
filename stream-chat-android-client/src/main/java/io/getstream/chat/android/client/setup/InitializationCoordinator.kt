package io.getstream.chat.android.client.setup

import io.getstream.chat.android.client.models.User

internal class InitializationCoordinator private constructor() {

    private val userDisconnectedListeners: MutableList<(User?) -> Unit> = mutableListOf()
    private val userConnectedListeners: MutableList<(User) -> Unit> = mutableListOf()

    fun addUserConnectedListener(listener: (User) -> Unit) {
        userConnectedListeners.add(listener)
    }

    fun addUserDisconnectedListener(listener: (User?) -> Unit) {
        userDisconnectedListeners.add(listener)
    }

    fun userConnected(user: User) {
        userConnectedListeners.forEach { function -> function.invoke(user) }
    }

    fun userDisconnected(user: User) {
        userConnectedListeners.forEach { function -> function.invoke(user) }
    }

    companion object {
        private var instance: InitializationCoordinator? = null

        fun getOrCreate(): InitializationCoordinator =
            instance ?: InitializationCoordinator().also { instance = it }
    }
}
