package io.getstream.chat.ui.sample.application

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.ChatLoggerHandler

object FirebaseLogger : ChatLoggerHandler {
    private val logger = ChatLogger.get("FirebaseLogger")
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    var userId: String? = null
        set(value) {
            field = value
            value?.let(crashlytics::setUserId)
        }

    private fun log(tag: Any? = null, message: String? = null, error: Throwable? = null) {
        if (tag == null && message == null && error == null) {
            logger.logD("No data provided; skipping Crashlytics logging")
            return
        }

        val logTag = tag ?: ""
        val logMsg = message ?: ""

        crashlytics.log("[$logTag] $logMsg")
        error?.let(crashlytics::recordException)
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
