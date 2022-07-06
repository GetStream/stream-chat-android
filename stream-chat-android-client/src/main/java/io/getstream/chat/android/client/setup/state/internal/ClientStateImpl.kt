package io.getstream.chat.android.client.setup.state.internal

import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientMutableState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class ClientStateImpl : ClientMutableState {

    private val _initialized = MutableStateFlow(false)
    private val _connectionState = MutableStateFlow(ConnectionState.OFFLINE)
    private val _user = MutableStateFlow<User?>(null)

    override val user: StateFlow<User?> = _user

    override val initialized: StateFlow<Boolean> = _initialized

    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override fun isOnline(): Boolean = _connectionState.value == ConnectionState.CONNECTED

    override fun isOffline(): Boolean = _connectionState.value == ConnectionState.OFFLINE

    override fun isConnecting(): Boolean = _connectionState.value == ConnectionState.CONNECTING

    override fun isInitialized(): Boolean = _initialized.value

    override fun clearState() {
        _initialized.value = false
        _connectionState.value = ConnectionState.OFFLINE
        _user.value = null
    }

    override fun setUser(user: User) {
        _user.value = user
    }

    override fun setConnectionState(connectionState: ConnectionState) {
        _connectionState.value = connectionState
    }

    override fun setInitialized(initialized: Boolean) {
        _initialized.value = initialized
    }
}
