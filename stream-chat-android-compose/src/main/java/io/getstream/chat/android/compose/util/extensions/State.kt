package io.getstream.chat.android.compose.util.extensions

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal fun <T> StateFlow<T>.asState(coroutineScope: CoroutineScope) : State<T> {
    val state = mutableStateOf(this.value)
    onEach { state.value = it }.launchIn(coroutineScope)
    return state
}