package io.getstream.chat.sample.application

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import timber.log.Timber

object FirebaseLogger : ChatLoggerHandler {

    private val logger: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    var userId: String? = null
        set(value) {
            field = value
            value?.let(logger::setUserId)
        }

    private fun log(tag: Any? = null, message: String? = null, error: Throwable? = null) {
        if (tag == null && message == null && error == null) {
            Timber.d("No data provided; skipping Crashlytics logging")
            return
        }

        val logTag = tag ?: ""
        val logMsg = message ?: ""

        logger.log("[$logTag] $logMsg")
        error?.let(logger::recordException)
    }

    override fun logD(tag: Any, message: String) {
        log(tag, message)
    }

    override fun logE(tag: Any, message: String) {
        log(tag, message)
    }

    override fun logE(tag: Any, message: String, throwable: Throwable) {
        log(tag, message, throwable)
    }

    override fun logI(tag: Any, message: String) {
        log(tag, message)
    }

    override fun logT(tag: Any, throwable: Throwable) {
        log(tag, error = throwable)
    }

    override fun logT(throwable: Throwable) {
        log(error = throwable)
    }

    override fun logW(tag: Any, message: String) {
        log(tag, message)
    }
}
