package io.getstream.chat.android.client.errors

enum class ChatErrorCode(val code: Int, val description: String) {

    /**
     * Local
     */
    NETWORK_FAILED(1000, "Response is failed. See cause"),
    PARSER_ERROR(1001, "Unable to parse error"),
    SOCKET_CLOSED(1002, "Server closed connection"),
    SOCKET_FAILURE(1003, "Listener.onFailure error"),
    CANT_PARSE_CONNECTION_EVENT(1004, "Unable to parse connection event"),
    CANT_PARSE_EVENT(1005, "Unable to parse event"),
    INVALID_TOKEN(1006, "Invalid token"),
    UNDEFINED_TOKEN(1007, "No defined token. Check if client.setUser was called"),


    /**
     * Backend
     */
    TOKEN_EXPIRED(40, "token expired, new one must be requested")
}