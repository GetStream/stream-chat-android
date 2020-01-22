package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.call.ChatCallImpl
import io.getstream.chat.android.core.poc.library.errors.ChatHttpError
import io.getstream.chat.android.core.poc.library.socket.ErrorResponse
import retrofit2.Response

class RetrofitCallMapper {

    fun <T> map(call: retrofit2.Call<T>): ChatCall<T> {

        return object : ChatCallImpl<T>() {

            override fun execute(): Result<T> {
                return execute(call)
            }

            override fun enqueue(callback: (Result<T>) -> Unit) {
                enqueue(call, callback)
            }

            override fun cancel() {
                super.cancel()
                call.cancel()
            }
        }
    }

    companion object {

        private fun <T> execute(call: retrofit2.Call<T>): Result<T> {
            try {
                val execute = call.execute()
                return Result(execute.body(), null)
            } catch (t: Throwable) {
                return failedResult(t)
            }
        }

        private fun <T> enqueue(call: retrofit2.Call<T>, callback: (Result<T>) -> Unit) {
            call.enqueue(object : retrofit2.Callback<T> {

                override fun onResponse(call: retrofit2.Call<T>, response: Response<T>) {
                    callback(getResult(response))
                }

                override fun onFailure(call: retrofit2.Call<T>, t: Throwable) {
                    callback(failedResult(t))
                }
            })
        }

        private fun <T> failedResult(t: Throwable): Result<T> {
            return Result(null, failedError(t))
        }

        private fun failedError(t: Throwable): ChatHttpError {

            var statusCode = -1
            var streamCode = -1

            if (t is ErrorResponse) {
                statusCode = t.statusCode
                streamCode = t.code
            }

            return ChatHttpError(streamCode, statusCode, t.message.toString(), t)
        }


        private fun <T> getResult(response: Response<T>): Result<T> {

            var data: T? = null
            var error: ChatHttpError? = null

            try {
                data = response.body()
            } catch (t: Throwable) {
                error = failedError(t)
            }

            return Result(data, error)
        }

//        private fun <A, B> callMapper(
//            retroCall: retrofit2.Call<A>,
//            chatCall: Call<A>,
//            mapper: (A) -> B
//        ): Call<B> {
//
//            return object : Call<B> {
//
//                override fun execute(): Result<B> {
//                    val result = retroCall.execute()
//                    return if (result.isSuccessful) {
//                        Result(mapper(result.body()!!), null)
//                    } else {
//                        Result(null,
//                            ClientError(
//                                result.code(),
//                                result.message()
//                            )
//                        )
//                    }
//                }
//
//                override fun enqueue(callback: (Result<B>) -> Unit) {
//                    retroCall.enqueue(object : retrofit2.Callback<A> {
//
//                        override fun onResponse(call: retrofit2.Call<A>, response: Response<A>) {
//                            val aResult = getResult(response)
//                            if (aResult.isSuccess()) {
//                                val b = mapper(aResult.data())
//                                callback(Result(b, null))
//                            } else {
//                                callback(Result(null, aResult.error))
//                            }
//
//                        }
//
//                        override fun onFailure(call: retrofit2.Call<A>, t: Throwable) {
//                            callback(Result(null,
//                                ClientError(
//                                    0,
//                                    "",
//                                    t
//                                )
//                            ))
//                        }
//                    })
//                }
//
//                override fun cancel() {
//                    chatCall.cancel()
//                }
//
//                override fun <C> map(mapperToC: (B) -> C): Call<C> {
//                    val toB = callMapper(retroCall, chatCall, mapper)
//                    return toB.map(mapperToC)
//                }
//
//
//            }
//        }
    }

}