package io.getstream.chat.android.core.poc.library

import retrofit2.Response

class RetrofitCallMapper {

    fun <T> map(call: retrofit2.Call<T>): Call<T> {

        return object : Call<T> {

            override fun execute(): Result<T> {
                return Result(call.execute().body(), null)
            }

            override fun enqueue(callback: (Result<T>) -> Unit) {
                call.enqueue(object : retrofit2.Callback<T> {

                    override fun onResponse(call: retrofit2.Call<T>, response: Response<T>) {
                        callback(getResult(response))
                    }

                    override fun onFailure(call: retrofit2.Call<T>, t: Throwable) {
                        callback(Result(null, ClientError(0, "", t)))
                    }
                })
            }

        }
    }

    private fun <T> getResult(response: Response<T>): Result<T> {

        var data: T? = null
        var error: Throwable? = null

        try {
            if (response.isSuccessful) {
                data = response.body()
            } else {
                error = RuntimeException("Network call error: ${response.code()}")
            }
        } catch (t: Throwable) {
            error = t
        }

        return Result(data, ClientError(0, "", error))
    }

}