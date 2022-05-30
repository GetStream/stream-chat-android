package io.getstream.chat.android.client.api.models.querysort

import io.getstream.chat.android.client.api.models.querysort.QuerySort.Companion.EQUAL_ON_COMPARISON
import io.getstream.chat.android.client.api.models.querysort.QuerySort.Companion.LESS_ON_COMPARISON
import io.getstream.chat.android.client.api.models.querysort.QuerySort.Companion.MORE_ON_COMPARISON

internal fun compare(
    first: Comparable<Any>?,
    second: Comparable<Any>?,
    sortDirection: SortDirection,
): Int {
    return when {
        first == null && second == null -> EQUAL_ON_COMPARISON
        first == null && second != null -> LESS_ON_COMPARISON * sortDirection.value
        first != null && second == null -> MORE_ON_COMPARISON * sortDirection.value
        first != null && second != null -> first.compareTo(second) * sortDirection.value
        else -> error("Impossible case!")
    }
}
