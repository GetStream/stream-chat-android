package io.getstream.chat.android.client.api.models.internal

import io.getstream.chat.android.client.api.models.QuerySort

internal class CompositeComparator<T>(private val comparators: List<Comparator<T>>) : Comparator<T> {
    override fun compare(o1: T, o2: T): Int =
        comparators.fold(QuerySort.EQUAL_ON_COMPARISON) { currentComparisonValue, comparator ->
            when (currentComparisonValue) {
                QuerySort.EQUAL_ON_COMPARISON -> comparator.compare(o1, o2)
                else -> currentComparisonValue
            }
        }
}
