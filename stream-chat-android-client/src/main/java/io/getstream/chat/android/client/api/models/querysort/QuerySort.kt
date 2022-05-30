package io.getstream.chat.android.client.api.models.querysort

public interface QuerySort<T : Any> {

    public val comparator: Comparator<in T>

    public fun toDto(): List<Map<String, Any>>

    public companion object {
        public const val KEY_DIRECTION: String = "direction"
        public const val KEY_FIELD_NAME: String = "field"
        public const val MORE_ON_COMPARISON: Int = 1
        public const val EQUAL_ON_COMPARISON: Int = 0
        public const val LESS_ON_COMPARISON: Int = -1
    }
}
