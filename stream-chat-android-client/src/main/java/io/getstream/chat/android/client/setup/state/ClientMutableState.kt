package io.getstream.chat.android.client.setup.state

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.internal.ClientStateImpl

public interface ClientMutableState : ClientState {

    public fun setUser(user: User)

    public fun setConnectionState(connectionState: ConnectionState)

    public fun setInitialized(initialized: Boolean)

    /**
     * Clears the state of [ClientState].
     */
    public fun clearState()

    public companion object {
        private var instance: ClientMutableState? = null

        public fun get(): ClientMutableState =
            instance ?: create().also { clientState ->
                instance = clientState
            }

        @VisibleForTesting
        public fun create(): ClientMutableState = ClientStateImpl()
    }
}
