package io.getstream.chat.android.core.poc.library.call

import io.getstream.chat.android.core.poc.library.Result

abstract class ChatCallImpl<T> : ChatCall<T> {

    protected var canceled = false

    abstract override fun execute(): Result<T>

    abstract override fun enqueue(callback: (Result<T>) -> Unit)

    override fun cancel() {
        canceled = true
    }

    override fun <K> map(mapper: (T) -> K): ChatCall<K> {
        return callMapper(this, mapper)
    }

    companion object {
        private fun <A, B> callMapper(
            callA: ChatCall<A>,
            mapper: (A) -> B
        ): ChatCallImpl<B> {
            return object : ChatCallImpl<B>() {
                override fun execute(): Result<B> {

                    val resultA = callA.execute()

                    return if (resultA.isSuccess) {
                        Result(mapper(resultA.data()), null)
                    } else {
                        Result(null, resultA.error())
                    }
                }

                override fun enqueue(callback: (Result<B>) -> Unit) {
                    callA.enqueue {

                        if (!canceled) {
                            if (it.isSuccess) {
                                callback(Result(mapper(it.data()), null))
                            } else {
                                callback(Result(null, it.error()))
                            }
                        }
                    }
                }
            }
        }
    }
}