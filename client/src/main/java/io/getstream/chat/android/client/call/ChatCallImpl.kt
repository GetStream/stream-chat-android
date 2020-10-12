package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

internal abstract class ChatCallImpl<T> : Call<T> {

    @Volatile
    protected var canceled = false
    protected var errorHandler: ((ChatError) -> Unit)? = null
    protected var nextHandler: ((T) -> Unit)? = null

    abstract override fun execute(): Result<T>

    abstract override fun enqueue(callback: (Result<T>) -> Unit)

    override fun cancel() {
        canceled = true
    }

    override fun <K> map(mapper: (T) -> K): Call<K> {
        return callMapper(this, mapper)
    }

    override fun onSuccess(handler: (T) -> Unit): Call<T> {
        nextHandler = handler
        return this
    }

    override fun onError(handler: (ChatError) -> Unit): Call<T> {
        errorHandler = handler
        return this
    }

    override fun <K> zipWith(call: Call<K>): Call<Pair<T, K>> {
        return ZipCall.zip(this, call)
    }

    override fun <K, P> zipWith(callK: Call<K>, callP: Call<P>): Call<Triple<T, K, P>> {
        return ZipCall.zip(this, callK, callP)
    }

    internal companion object {
        private fun <A, B> callMapper(
            callA: Call<A>,
            mapper: (A) -> B
        ): ChatCallImpl<B> {
            return object : ChatCallImpl<B>() {

                override fun execute(): Result<B> {

                    val resultA = callA.execute()

                    return if (resultA.isSuccess) {
                        val data = mapper(resultA.data())
                        nextHandler?.invoke(data)
                        Result(data, null)
                    } else {
                        val error = resultA.error()
                        errorHandler?.invoke(error)
                        Result(null, error)
                    }
                }

                override fun enqueue(callback: (Result<B>) -> Unit) {
                    callA.enqueue {

                        if (!canceled) {
                            if (it.isSuccess) {
                                val data = mapper(it.data())
                                nextHandler?.invoke(data)
                                callback(Result(data, null))
                            } else {
                                val error = it.error()
                                errorHandler?.invoke(error)
                                callback(Result(null, error))
                            }
                        }
                    }
                }
            }
        }
    }
}
