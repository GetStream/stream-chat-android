package io.getstream.chat.android.compose.util.extensions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn

// TODO
internal fun <T> StateFlow<T>.asState(coroutineScope: CoroutineScope) : State<T> {
    val state = mutableStateOf(this.value)
    onEach { state.value = it }.launchIn(coroutineScope)
    return state
}

internal fun <T> Flow<T>.asState(coroutineScope: CoroutineScope, defaultValue: T) : State<T> {
    val state = mutableStateOf(defaultValue)
    onEach { state.value = it }.launchIn(coroutineScope)
    return state
}
