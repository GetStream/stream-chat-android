package io.getstream.chat.test

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.utils.Result

public fun <T : Any> callFrom(valueProvider: () -> T): Call<T> = TestCall(Result(valueProvider()))

public fun <T : Any> T.asCall(): Call<T> = TestCall(Result(this))
