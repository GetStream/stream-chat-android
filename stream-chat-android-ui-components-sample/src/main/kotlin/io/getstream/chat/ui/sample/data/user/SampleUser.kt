package io.getstream.chat.ui.sample.data.user

data class SampleUser(
    val id: String,
    val name: String,
    val token: String,
) {

    val image: String
        get() = "https://getstream.io/random_png?id=$id&name=$name"

    companion object {
        val None: SampleUser = SampleUser("", "", "")
    }
}
