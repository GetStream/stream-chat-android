package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.utils.Result
import retrofit2.Response

class RetrofitCall<T>(val call: retrofit2.Call<T>, val parser: ChatParser) : ChatCallImpl<T>() {
    override fun execute(): Result<T> {
        val result = execute(call)
        if (!result.isSuccess) errorHandler?.invoke(result.error())
        else nextHandler?.invoke(result.data())
        return result
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        enqueue(call) {
            if (!canceled) {
                if (!it.isSuccess) errorHandler?.invoke(it.error())
                else nextHandler?.invoke(it.data())
                callback(it)
            }
        }
    }

    override fun enqueue() {
        enqueue {
            // ignore result
        }
    }

    override fun cancel() {
        super.cancel()
        call.cancel()
    }

    private fun execute(call: retrofit2.Call<T>): Result<T> {
        return getResult(call)
    }

    private fun enqueue(call: retrofit2.Call<T>, callback: (Result<T>) -> Unit) {
        call.enqueue(object : retrofit2.Callback<T> {

            override fun onResponse(call: retrofit2.Call<T>, response: Response<T>) {
                callback(getResult(response))
            }

            override fun onFailure(call: retrofit2.Call<T>, t: Throwable) {
                callback(failedResult(t))
            }
        })
    }

    private fun failedResult(t: Throwable): Result<T> {
        return Result(null, failedError(t))
    }

    private fun failedError(t: Throwable): ChatNetworkError {

        var statusCode = -1
        var streamCode = -1

        return ChatNetworkError(t.message.toString(), t, streamCode, statusCode)
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

        var data: T? = null
        var error: ChatNetworkError? = null

        if (retrofitResponse.isSuccessful) {
            try {
                data = retrofitResponse.body()
            } catch (t: Throwable) {
                error = failedError(t)
            }
        } else {
            error = parser.toError(retrofitResponse.raw())
        }

        return Result(data, error)
    }


}