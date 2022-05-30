package io.getstream.chat.android.client.api.models.querysort

public data class SortSpecification<T>(
    val sortAttribute: SortAttribute<T>,
    val sortDirection: SortDirection,
)
