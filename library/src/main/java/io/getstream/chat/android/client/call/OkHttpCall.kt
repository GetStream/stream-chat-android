package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.utils.Result
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.io.InputStream

class OkHttpCall<T>(val call: Call, val converter: (InputStream) -> T) : ChatCallImpl<T>() {

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

    override fun cancel() {
        super.cancel()
        call.cancel()
    }

    private fun execute(call: Call): Result<T> {
        return getResult(call)
    }

    private fun enqueue(call: Call, callback: (Result<T>) -> Unit) {

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(failedResult(e))
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                callback(getResult(response))
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

    private fun getResult(retroCall: Call): Result<T> {
        return try {
            val retrofitResponse = retroCall.execute()
            getResult(retrofitResponse)
        } catch (t: Throwable) {
            failedResult(t)
        }
    }

    private fun getResult(response: okhttp3.Response): Result<T> {

        var data: T? = null
        var error: ChatNetworkError? = null

        if (response.isSuccessful) {
            try {
                val stream = response.body!!.byteStream()
                data = converter(stream)
            } catch (t: Throwable) {
                error = failedError(t)
            }
        } else {
            val code = response.code
            error = ChatNetworkError("Response is failed: code: $code", null, -1, code)
        }

        return Result(data, error)
    }
}