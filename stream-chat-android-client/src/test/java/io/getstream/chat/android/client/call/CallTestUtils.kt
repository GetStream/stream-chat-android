package io.getstream.chat.android.client.call

internal fun <T : Any> callFrom(valueProvider: () -> T): Call<T> = WrappedValueCall(valueProvider())

internal fun <T : Any> T.asCall(): Call<T> = WrappedValueCall(this)
