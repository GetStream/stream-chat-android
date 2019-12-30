package io.getstream.chat.android.core.poc.library.requests

sealed class Field(val field: String) {

    object cid : Field("cid")
    object user_id : Field("user_id")

    override fun toString(): String {
        return field
    }
}