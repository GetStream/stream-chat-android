package io.getstream.chat.android.core.poc.library

import retrofit2.Response

class RetrofitCallMapper {

    fun <T> map(call: retrofit2.Call<T>): Call<T> {

        return object : Call<T> {

            override fun execute(): Result<T> {
                return execute(call)
            }

            override fun enqueue(callback: (Result<T>) -> Unit) {
                enqueue(call, callback)
            }

            override fun <K> map(mapper: (T) -> K): Call<K> {
                return callMapper(call, this, mapper)
            }

            override fun cancel() {
                call.cancel()
            }
        }
    }

    companion object {

        private fun <T> execute(call: retrofit2.Call<T>): Result<T> {
            val execute = call.execute()
            return Result(execute.body(), null)
        }

        private fun <T> enqueue(call: retrofit2.Call<T>, callback: (Result<T>) -> Unit) {
            call.enqueue(object : retrofit2.Callback<T> {

                override fun onResponse(call: retrofit2.Call<T>, response: Response<T>) {
                    callback(getResult(response))
                }

                override fun onFailure(call: retrofit2.Call<T>, t: Throwable) {
                    callback(Result(null, ClientError(0, "", t)))
                }
            })
        }

        private fun <T> getResult(response: Response<T>): Result<T> {

            var data: T? = null
            var error: ClientError? = null

            try {
                if (response.isSuccessful) {
                    data = response.body()
                } else {
                    error = ClientError(response.code(), "Network call error: ${response.code()}")
                }
            } catch (t: Throwable) {
                error = ClientError(-1, t.message.toString(), t)
            }

            return Result(data, error)
        }

        private fun <A, B> callMapper(
            retroCall: retrofit2.Call<A>,
            chatCall: Call<A>,
            mapper: (A) -> B
        ): Call<B> {

            return object : Call<B> {

                override fun execute(): Result<B> {
                    val result = retroCall.execute()
                    return if (result.isSuccessful) {
                        Result(mapper(result.body()!!), null)
                    } else {
                        Result(null, ClientError(result.code(), result.message()))
                    }
                }

                override fun enqueue(callback: (Result<B>) -> Unit) {
                    retroCall.enqueue(object : retrofit2.Callback<A> {

                        override fun onResponse(call: retrofit2.Call<A>, response: Response<A>) {
                            val aResult = getResult(response)
                            if (aResult.isSuccess()) {
                                val b = mapper(aResult.data())
                                callback(Result(b, null))
                            } else {
                                callback(Result(null, aResult.error))
                            }

                        }

                        override fun onFailure(call: retrofit2.Call<A>, t: Throwable) {
                            callback(Result(null, ClientError(0, "", t)))
                        }
                    })
                }

                override fun cancel() {
                    chatCall.cancel()
                }

                override fun <C> map(mapperToC: (B) -> C): Call<C> {
                    val toB = callMapper(retroCall, chatCall, mapper)
                    return toB.map(mapperToC)
                }


            }
        }
    }

}