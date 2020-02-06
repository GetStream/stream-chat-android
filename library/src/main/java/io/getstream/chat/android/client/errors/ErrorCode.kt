package io.getstream.chat.android.client.errors

enum class ErrorCode(val value: Int, val description: String) {
    TOKEN_EXPIRED(40, "token expired, new one must be requested")
}