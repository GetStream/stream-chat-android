package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.call.ChatCallImpl
import io.getstream.chat.android.client.utils.Result

internal class FileUploaderCall<T : Any>(private val call: () -> Result<T>) : ChatCallImpl<T>() {

    override fun execute(): Result<T> = call()

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(call())
    }
}
