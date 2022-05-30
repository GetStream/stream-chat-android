package io.getstream.chat.android.client.api.models.querysort

/** Sort order which can be ascending or descending. */
public enum class SortDirection(public val value: Int) {
    /** Descending sort order. */
    DESC(-1),

    /** Ascending sort order. */
    ASC(1)
}
