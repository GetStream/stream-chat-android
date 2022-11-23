package io.getstream.android.push;

/**
 * Push notifications provider type.
 */
public enum class PushProvider(public val key: String) {
    /** Firebase push notification provider */
    FIREBASE("firebase"),
    /** Huawei push notification provider */
    HUAWEI("huawei"),
    /** Xiaomi push notification provider */
    XIAOMI("xiaomi"),
    /** Unknown push notification provider */
    UNKNOWN("unknown");

    public companion object {
        public fun fromKey(key: String): PushProvider =
            values().firstOrNull { it.key == key } ?: UNKNOWN
    }
}