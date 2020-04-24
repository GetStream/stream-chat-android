package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.logger.ChatLoggerHandler

class TestLoggerHandler : ChatLoggerHandler {
    override fun logT(throwable: Throwable) {
        System.out.println("logT: $throwable")
    }

    override fun logT(tag: Any, throwable: Throwable) {
        System.out.println("logT: $throwable")
    }

    override fun logI(tag: Any, message: String) {
        System.out.println("logI: $tag $message")
    }

    override fun logD(tag: Any, message: String) {
        System.out.println("logD: $tag $message")
    }

    override fun logW(tag: Any, message: String) {
        System.out.println("logW: $tag $message")
    }

    override fun logE(tag: Any, message: String) {
        System.out.println("logE: $tag $message")
    }

    override fun logE(tag: Any, message: String, throwable: Throwable) {
        System.out.println("logE: $tag $message $throwable")
    }
}
