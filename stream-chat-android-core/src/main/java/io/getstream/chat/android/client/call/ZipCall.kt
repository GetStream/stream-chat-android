package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class ZipCall<A : Any, B : Any>(
    private val callA: Call<A>,
    private val callB: Call<B>
) : Call<Pair<A, B>> {
    private var job: Job? = null

    override fun cancel() {
        job?.cancel()
    }

    override fun execute(): Result<Pair<A, B>> {
        val job = Job()
        this.job = job

        return runBlocking(job) {
            val deferredA = async { callA.await() }
            val deferredB = async { callB.await() }

            val resultA = deferredA.await()
            if (resultA.isError) {
                deferredB.cancel()
                return@runBlocking getErrorA(resultA)
            }

            val resultB = deferredB.await()
            if (resultB.isError) {
                return@runBlocking getErrorB(resultB)
            }

            Result(Pair(resultA.data(), resultB.data()))
        }
    }

    override fun enqueue(callback: Call.Callback<Pair<A, B>>) {
        suspend fun performCallback(result: Result<Pair<A, B>>) {
            withContext(DispatcherProvider.Main) { callback.onResult(result) }
        }

        job = GlobalScope.launch {
            val deferredA = async { callA.await() }
            val deferredB = async { callB.await() }

            val resultA = deferredA.await()
            if (resultA.isError) {
                deferredB.cancel()
                performCallback(getErrorA(resultA))
                return@launch
            }

            val resultB = deferredB.await()
            if (resultB.isError) {
                performCallback(getErrorB(resultB))
                return@launch
            }

            performCallback(Result(Pair(resultA.data(), resultB.data())))
        }
    }

    private fun <A : Any, B : Any> getErrorA(resultA: Result<A>): Result<Pair<A, B>> {
        return Result(ChatError("Error executing callA", resultA.error().cause))
    }

    private fun <A : Any, B : Any> getErrorB(resultB: Result<B>): Result<Pair<A, B>> {
        return Result(ChatError("Error executing callB", resultB.error().cause))
    }
}
