package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.logger.ChatLoggerHandler

class TestLoggerHandler : ChatLoggerHandler {
    override fun logT(throwable: Throwable) {
        println("logT: $throwable")
    }

    override fun logT(tag: Any, throwable: Throwable) {
        println("logT: $throwable")
    }

    override fun logI(tag: Any, message: String) {
        println("logI: $tag $message")
    }

    override fun logD(tag: Any, message: String) {
        println("logD: $tag $message")
    }

    override fun logW(tag: Any, message: String) {
        println("logW: $tag $message")
    }

    override fun logE(tag: Any, message: String) {
        println("logE: $tag $message")
    }

    override fun logE(tag: Any, message: String, throwable: Throwable) {
        println("logE: $tag $message $throwable")
    }
}
