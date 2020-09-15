package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.errors.ChatRequestError
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
        var error: ChatError? = null

        if (response.isSuccessful) {
            try {
                val stream = response.body!!.byteStream()
                data = converter(stream)
            } catch (t: Throwable) {
                error = failedError(t)
            }
        } else {
            val code = response.code
            error = ChatNetworkError.create(ChatErrorCode.NETWORK_FAILED, null, code)
        }

        return Result(data, error)
    }
}
