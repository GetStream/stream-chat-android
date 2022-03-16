package io.getstream.chat.android.offline.model.connection

/**
 * Represents possible states of the WebSocket connection.
 */
public enum class ConnectionState {
    /**
     * The client is connected to the WebSocket.
     */
    CONNECTED,

    /**
     * The client is trying to connect to the WebSocket.
     */
    CONNECTING,

    /**
     * The client is permanently disconnected from the WebSocket.
     */
    OFFLINE
}
