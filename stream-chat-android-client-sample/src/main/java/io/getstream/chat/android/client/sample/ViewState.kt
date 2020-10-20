package io.getstream.chat.android.client.sample

sealed class ViewState<T> {
    data class Success<T>(val data: T) : ViewState<T>()
    data class Error<T>(val error: Throwable) : ViewState<T>()
    class Loading<T> : ViewState<T>()
}
