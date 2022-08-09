package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class ThreadCall<T : Any>(
    private val task: () -> T
): Call<T> {

    private var thread: Thread? = null

    override fun execute(): Result<T> = try {
        Result.success(task())
    } catch (e: Throwable) {
        Result.error(e)
    }

    override fun enqueue(callback: Call.Callback<T>) {
        thread = Thread { callback.onResult(execute()) }
        thread?.start()
    }

    override suspend fun await(): Result<T> {
        return execute()
    }

    override fun cancel() {
        thread?.interrupt()
    }
}
