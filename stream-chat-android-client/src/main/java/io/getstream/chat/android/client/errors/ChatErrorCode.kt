package io.getstream.chat.android.client.errors

public enum class ChatErrorCode(public val code: Int, public val description: String) {

    /**
     * Local
     */
    NETWORK_FAILED(1000, "Response is failed. See cause"),
    PARSER_ERROR(1001, "Unable to parse error"),
    SOCKET_CLOSED(1002, "Server closed connection"),
    SOCKET_FAILURE(1003, "See stack trace in logs. Intercept error in error handler of setUser"),
    CANT_PARSE_CONNECTION_EVENT(1004, "Unable to parse connection event"),
    CANT_PARSE_EVENT(1005, "Unable to parse event"),
    INVALID_TOKEN(1006, "Invalid token"),
    UNDEFINED_TOKEN(1007, "No defined token. Check if client.setUser was called and finished"),
    UNABLE_TO_PARSE_SOCKET_EVENT(1008, "Socket event payload either invalid or null"),
    NO_ERROR_BODY(1009, "No error body. See http status code"),

    /**
     * Backend
     */
    TOKEN_EXPIRED(40, "Token expired, new one must be requested."),
    API_KEY_NOT_FOUND(2, "Api key is not found, verify it if it's correct or was created.")
}
