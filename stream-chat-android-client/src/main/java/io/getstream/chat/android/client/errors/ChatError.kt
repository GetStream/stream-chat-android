package io.getstream.chat.android.client.errors

/**
 * The error response from the chat server.
 *
 * @property code The error code.
 * @property message The error message.
 * @property statusCode The status code.
 * @property exceptionFields The exception fields.
 * @property moreInfo More info about the error.
 * @property details The error details.
 * @property duration The duration of the error.
 */
public data class ChatError(
    val code: Int = -1,
    var message: String = "",
    var statusCode: Int = -1,
    val exceptionFields: Map<String, String> = mapOf(),
    var moreInfo: String = "",
    val details: List<ChatErrorDetail> = emptyList(),
    var duration: String = ""
)

/**
 * The error detail.
 *
 * @property code The error code.
 * @property messages The error messages.
 */
public data class ChatErrorDetail(
    public val code: Int,
    public val messages: List<String>,
)