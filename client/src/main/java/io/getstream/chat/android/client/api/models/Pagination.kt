package io.getstream.chat.android.client.api.models

enum class Pagination(private val value: String) {

    GREATER_THAN("id_gt"),
    GREATER_THAN_OR_EQUAL("id_gte"),
    LESS_THAN("id_lt"),
    LESS_THAN_OR_EQUAL("id_lte");

    fun get(): String {
        return value
    }

    override fun toString(): String {
        return this.get()
    }
}
