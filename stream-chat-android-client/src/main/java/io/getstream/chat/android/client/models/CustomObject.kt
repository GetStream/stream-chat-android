package io.getstream.chat.android.client.models

public sealed interface CustomObject {
    public var extraData: MutableMap<String, Any>

    @Suppress("UNCHECKED_CAST")
    public fun <T> getExtraValue(key: String, default: T): T {
        return if (extraData.containsKey(key)) {
            extraData[key] as T
        } else {
            default
        }
    }

    public fun putExtraValue(key: String, value: Any) {
        extraData[key] = value
    }
}
