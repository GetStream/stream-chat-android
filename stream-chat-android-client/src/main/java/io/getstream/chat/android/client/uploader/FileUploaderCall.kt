package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.call.ChatCallImpl
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class FileUploaderCall<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val call: () -> Result<T>
) : ChatCallImpl<T>() {

    override fun execute(): Result<T> = call()

    override fun enqueue(callback: (Result<T>) -> Unit) {
        coroutineScope.launch {
            val result = call()
            callback(result)
        }
    }
}
