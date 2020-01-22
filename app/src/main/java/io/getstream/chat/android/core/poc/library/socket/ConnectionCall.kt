package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.Result
import io.getstream.chat.android.core.poc.library.call.ChatCallImpl
import io.getstream.chat.android.core.poc.library.errors.SocketChatError

class ConnectionCall : ChatCallImpl<ConnectionData>() {

    lateinit var callback: (Result<ConnectionData>) -> Unit

    fun deliverResult(connection: ConnectionData, error: Throwable?) {

        if (canceled) return

        if (error == null) {
            callback(
                Result(
                    connection,
                    null
                )
            )
        } else {
            callback(
                Result(
                    connection,
                    SocketChatError(
                        "Connection error",
                        error
                    )
                )
            )
        }
    }

    override fun execute(): Result<ConnectionData> {
        return Result(
            null,
            SocketChatError("Sync socket connection is not supported")
        )
    }

    override fun enqueue(callback: (Result<ConnectionData>) -> Unit) {
        this.callback = callback
    }

}