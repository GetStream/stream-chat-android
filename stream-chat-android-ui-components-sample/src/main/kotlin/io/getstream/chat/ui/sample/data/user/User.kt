package io.getstream.chat.ui.sample.data.user

data class User(
    val id: String,
    val name: String,
    val token: String,
    val image: String = "https://api.adorable.io/avatars/285/$id.png"
) {

    companion object {
        val None: User = User("", "", "")
    }
}
