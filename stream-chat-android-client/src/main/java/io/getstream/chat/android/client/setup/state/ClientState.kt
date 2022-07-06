package io.getstream.chat.android.client.setup.state

import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.StateFlow

public interface ClientState {

    /**
     * The current user in the OfflinePlugin state.
     */
    public val user: StateFlow<User?>

    /**
     * If the client connection has been initialized.
     */
    public val initialized: StateFlow<Boolean>

    /**
     * StateFlow<ConnectionState> that indicates if we are currently online, connecting of offline.
     */
    public val connectionState: StateFlow<ConnectionState>


    /**
     * If the user is online or not.
     *
     * @return True if the user is online otherwise False.
     */
    public fun isOnline(): Boolean

    /**
     * If the user is offline or not.
     *
     * @return True if the user is offline otherwise False.
     */
    public fun isOffline(): Boolean

    /**
     * If connection is in connecting state.
     *
     * @return True if the connection is in connecting state.
     */
    public fun isConnecting(): Boolean

    /**
     * If domain state is initialized or not.
     *
     * @return True if initialized otherwise False.
     */
    public fun isInitialized(): Boolean

}
