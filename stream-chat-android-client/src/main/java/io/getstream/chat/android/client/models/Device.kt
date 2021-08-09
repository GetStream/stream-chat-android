package io.getstream.chat.android.client.models

public data class Device(
    val token: String,
    var pushProvider: PushProvider,
)

public enum class PushProvider(internal val key: String) {
    FIREBASE("firebase"),
    HUAWEI("huawei"),
    UNKNOWN("unknown");

    internal companion object {
        internal fun fromKey(key: String): PushProvider =
            values().firstOrNull { it.key == key } ?: UNKNOWN
    }
}
