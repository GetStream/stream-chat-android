package io.getstream.chat.android.livedata.service.sync

data class BackgroundSyncConfig(
    val apiKey: String,
    val userId: String,
    val userToken: String
) {
    companion object {
        val UNAVAILABLE = BackgroundSyncConfig("", "", "")
    }
}