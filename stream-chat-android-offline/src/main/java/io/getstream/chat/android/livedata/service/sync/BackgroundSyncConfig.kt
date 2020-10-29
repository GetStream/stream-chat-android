package io.getstream.chat.android.livedata.service.sync

internal data class BackgroundSyncConfig(
    val apiKey: String,
    val userId: String,
    val userToken: String
) {
    public fun isValid(): Boolean {
        return (apiKey.isNotEmpty() && userId.isNotEmpty() && userToken.isNotEmpty())
    }
}
