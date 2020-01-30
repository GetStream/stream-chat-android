package io.getstream.chat.android.core.poc.library.logger

class StreamChatSilentLogger : StreamLogger {
    override fun logT(throwable: Throwable) {
        // unused
    }

    override fun logT(tag: Any, throwable: Throwable) {
        // unused
    }

    override fun logI(tag: Any, message: String) {
        // unused
    }

    override fun logD(tag: Any, message: String) {
        // unused
    }

    override fun logW(tag: Any, message: String) {
        // unused
    }

    override fun logE(tag: Any, message: String) {
        // unused
    }
}