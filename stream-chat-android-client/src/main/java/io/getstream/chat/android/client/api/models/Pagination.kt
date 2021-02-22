package io.getstream.chat.android.client.api.models

public enum class Pagination(private val value: String) {
    GREATER_THAN("id_gt"),
    GREATER_THAN_OR_EQUAL("id_gte"),
    LESS_THAN("id_lt"),
    LESS_THAN_OR_EQUAL("id_lte"),
    ;

    override fun toString(): String {
        return value
    }
}
