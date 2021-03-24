package io.getstream.chat.ui.sample.data.user

data class SampleUser(
    val apiKey: String,
    val id: String,
    val name: String,
    val token: String,
    val image: String,
) {

    companion object {
        val None: SampleUser = SampleUser("", "", "", "", "https://getstream.io/random_png?id=none&name=none&size=200")
    }
}
