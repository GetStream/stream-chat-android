package io.getstream.chat.android.compose.viewmodel.channels.delegates

import io.getstream.chat.android.core.utils.Debouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.plus

internal interface IHelpSearchWithDebounce {
    val searchScope: CoroutineScope
    val chListScope: CoroutineScope
    val searchDebouncer: Debouncer
}

internal class StreamChannelSearchHelper(
    searchDebounceMs: Long,
    viewModelScope: CoroutineScope,
) : IHelpSearchWithDebounce {
    /**
     * The scope used for search operations.
     */
    override val searchScope = viewModelScope.let { it + SupervisorJob(it.coroutineContext.job) }

    /**
     * The scope used for channel list operations.
     */
    override val chListScope = viewModelScope.let { it + SupervisorJob(it.coroutineContext.job) }

    /**
     * The debouncer used for search operations.
     */
    override val searchDebouncer = Debouncer(searchDebounceMs, searchScope)
}