package io.getstream.chat.android.compose.viewmodel.channels.filtering

import io.getstream.chat.android.models.FilterObject
import kotlinx.coroutines.CoroutineScope

internal interface IFilterChannels {
    fun setupFilters(
        initialFilters: FilterObject?,
        viewModelScope: CoroutineScope,
    )
}
