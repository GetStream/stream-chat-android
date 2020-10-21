package io.getstream.chat.sample.application

import io.getstream.chat.android.client.logger.ChatLoggerHandler
import timber.log.Timber

class SampleLoggingHandler : ChatLoggerHandler {

    private fun Any.withPrefix() = "SampleLoggingHandler/$this"

    override fun logD(tag: Any, message: String) {
        Timber.tag(tag.withPrefix()).d(message)
    }

    override fun logE(tag: Any, message: String) {
        Timber.tag(tag.withPrefix()).e(message)
    }

    override fun logE(tag: Any, message: String, throwable: Throwable) {
        Timber.tag(tag.withPrefix()).e(throwable, message)
    }

    override fun logI(tag: Any, message: String) {
        Timber.tag(tag.withPrefix()).i(message)
    }

    override fun logT(tag: Any, throwable: Throwable) {
        Timber.tag(tag.withPrefix()).e(throwable)
    }

    override fun logT(throwable: Throwable) {
        Timber.e(throwable)
    }

    override fun logW(tag: Any, message: String) {
        Timber.tag(tag.withPrefix()).w(message)
    }
}
