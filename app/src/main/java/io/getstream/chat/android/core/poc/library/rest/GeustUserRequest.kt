package io.getstream.chat.android.core.poc.library.rest


class GuestUserRequest(id: String, name: String) {

    val user: GuestUserBody = GuestUserBody(id, name)

    data class GuestUserBody(val id: String, val name: String)
}
