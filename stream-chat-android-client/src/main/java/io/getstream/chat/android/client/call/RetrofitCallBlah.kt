package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.utils.Result
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

internal class RetrofitCallBlah<T : Any>(
    val call: retrofit2.Call<T>,
    private val parser: ChatParser,
    private val callbackExecutor: Executor
) : Call<T> {

    protected var canceled = AtomicBoolean(false)

    override fun cancel() {
        canceled.set(true)
        call.cancel()
    }

    override fun execute(): Result<T> {
        return execute(call)
    }

    override fun enqueue(callback: Call.Callback<T>) {
        enqueue(call) {
            if (!canceled.get()) {
                callback.onResult(it)
            }
        }
    }

    private fun execute(call: retrofit2.Call<T>): Result<T> {
        return getResult(call)
    }

    private fun enqueue(call: retrofit2.Call<T>, callback: (Result<T>) -> Unit) {
        call.enqueue(
            object : Callback<T> {
                override fun onResponse(call: retrofit2.Call<T>, response: Response<T>) {
                    callbackExecutor.execute {
                        callback(getResult(response))
                    }
                }

                override fun onFailure(call: retrofit2.Call<T>, t: Throwable) {
                    callbackExecutor.execute {
                        callback(failedResult(t))
                    }
                }
            }
        )
    }

    private fun failedResult(t: Throwable): Result<T> {
        return Result(failedError(t))
    }

    private fun failedError(t: Throwable): ChatError {
        return when (t) {
            is ChatError -> {
                t
            }
            is ChatRequestError -> {
                ChatNetworkError.create(t.streamCode, t.message.toString(), t.statusCode, t.cause)
            }
            else -> {
                ChatNetworkError.create(ChatErrorCode.NETWORK_FAILED, t)
            }
        }
    }

    private fun getResult(retroCall: retrofit2.Call<T>): Result<T> {
        return try {
            val retrofitResponse = retroCall.execute()
            getResult(retrofitResponse)
        } catch (t: Throwable) {
            failedResult(t)
        }
    }

    private fun getResult(retrofitResponse: Response<T>): Result<T> {
        return if (retrofitResponse.isSuccessful) {
            try {
                Result(retrofitResponse.body()!!)
            } catch (t: Throwable) {
                Result(failedError(t))
            }
        } else {
            Result(parser.toError(retrofitResponse.raw()))
        }
    }
}
