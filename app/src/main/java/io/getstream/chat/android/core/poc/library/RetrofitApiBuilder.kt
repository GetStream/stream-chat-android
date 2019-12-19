package io.getstream.chat.android.core.poc.library

import android.os.Handler
import android.os.Looper
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

/**
 * Fake api builder
 *
 * Emulates
 * - network delay
 * - error in case of running on UI thread
 */
class RetrofitApiBuilder {

    private val executor = Executors.newSingleThreadExecutor()

    fun build(): RetrofitApiService {

        return object : RetrofitApiService {
            override fun queryChannels(): Call<List<Channel>> {

                return object : Call<List<Channel>> {

                    override fun enqueue(callback: Callback<List<Channel>>) {
                        runOnBackground {
                            val result = execute()
                            runOnUiThread {
                                callback.onResponse(this, result)
                            }
                        }
                    }

                    override fun isExecuted(): Boolean {
                        return true
                    }

                    override fun clone(): Call<List<Channel>> {
                        return null!!
                    }

                    override fun isCanceled(): Boolean {
                        return false
                    }

                    override fun cancel() {

                    }

                    override fun execute(): Response<List<Channel>> {
                        emulateNetworkCall()
                        return Response.success(listOfChannels())
                    }

                    override fun request(): Request {
                        return null!!
                    }

                }
            }
        }
    }

    private fun emulateNetworkCall() {
        verifyThread()
        Thread.sleep(1000)
    }

    private fun verifyThread() {
        val currentThread = Thread.currentThread()
        val uiThread = Looper.getMainLooper().thread
        if (currentThread == uiThread) throw RuntimeException("Network call in main thread")
    }

    private fun runOnBackground(task: () -> Unit) {
        executor.submit(task)
    }

    private fun runOnUiThread(task: () -> Unit) {
        Handler(Looper.getMainLooper()).post { task() }
    }

    private fun listOfChannels(): List<Channel> {
        val count = 20
        val result = mutableListOf<Channel>()
        for (i in 1..count) {
            result.add(Channel(i.toString(), "name-rm-id: $i", i))
        }
        return result
    }
}