package io.getstream.chat.android.client.api

import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.RetrofitCallBlah
import io.getstream.chat.android.client.parser.ChatParser
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.Executor

internal class RetrofitCallAdapterFactory private constructor(
    private val chatParser: ChatParser,
    private val callbackExecutor: Executor
) : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != RetrofitCallBlah::class.java) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalArgumentException("Call return type must be parameterized as Call<Foo>")
        }
        val responseType: Type = getParameterUpperBound(0, returnType)
        return RetrofitCallAdapter<Any>(responseType, chatParser, callbackExecutor)
    }

    companion object {
        val mainThreadExecutor: Executor = object : Executor {
            val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
            override fun execute(command: Runnable?) {
                command?.let(handler::post)
            }
        }

        fun create(
            chatParser: ChatParser,
            callbackExecutor: Executor? = null,
        ): RetrofitCallAdapterFactory = RetrofitCallAdapterFactory(chatParser, callbackExecutor ?: mainThreadExecutor)
    }
}

internal class RetrofitCallAdapter<T : Any>(
    private val responseType: Type,
    private val parser: ChatParser,
    private val callbackExecutor: Executor
) : CallAdapter<T, Call<T>> {

    override fun adapt(call: retrofit2.Call<T>): Call<T> {
        return RetrofitCallBlah(call, parser, callbackExecutor)
    }

    override fun responseType(): Type = responseType
}
