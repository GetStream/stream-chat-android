package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result

abstract class ChatCallImpl<T> : Call<T> {

    protected var canceled = false

    abstract override fun execute(): Result<T>

    abstract override fun enqueue(callback: (Result<T>) -> Unit)

    override fun cancel() {
        canceled = true
    }

    override fun <K> map(mapper: (T) -> K): Call<K> {
        return callMapper(this, mapper)
    }

    companion object {
        private fun <A, B> callMapper(
            callA: Call<A>,
            mapper: (A) -> B
        ): ChatCallImpl<B> {
            return object : ChatCallImpl<B>() {
                override fun execute(): Result<B> {

                    val resultA = callA.execute()

                    return if (resultA.isSuccess) {
                        Result(
                            mapper(
                                resultA.data()
                            ), null
                        )
                    } else {
                        Result(
                            null,
                            resultA.error()
                        )
                    }
                }

                override fun enqueue(callback: (Result<B>) -> Unit) {
                    callA.enqueue {

                        if (!canceled) {
                            if (it.isSuccess) {
                                callback(
                                    Result(
                                        mapper(it.data()),
                                        null
                                    )
                                )
                            } else {
                                callback(
                                    Result(
                                        null,
                                        it.error()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}