package io.getstream.chat.android.client.logger

import android.util.Log
import io.getstream.chat.android.client.errors.ChatError
import java.io.PrintWriter
import java.io.StringWriter

private const val TAG_PREFIX = "Chat:"

internal class ChatLoggerImpl constructor(
    private val level: ChatLogLevel = ChatLogLevel.NOTHING,
    private val handler: ChatLoggerHandler? = null
) : ChatLogger {

    override fun logE(tag: Any, message: String, throwable: Throwable) {
        if (level.isMoreOrEqualsThan(ChatLogLevel.ERROR)) {
            Log.e(getTag(tag), message)
            Log.e(getTag(tag), getStackString(throwable))
        }
        handler?.logE(tag, message, throwable)
    }

    override fun logE(tag: Any, throwable: Throwable) {
        if (level.isMoreOrEqualsThan(ChatLogLevel.ERROR)) {
            Log.e(getTag(tag), getStackString(throwable))
        }
        handler?.logT(getTag(tag), throwable)
    }

    override fun logE(tag: Any, chatError: ChatError) {
        val cause = chatError.cause
        val message = chatError.message
        when {
            cause != null && message != null -> logE(
                tag,
                message,
                cause
            )
            cause != null -> logE(tag, cause)
            else -> logE(tag, message.orEmpty())
        }
    }

    override fun logE(tag: Any, message: String, chatError: ChatError) {
        val cause = chatError.cause
        if (cause != null) {
            logE(tag, message, cause)
        } else {
            logE(tag, message)
        }
    }

    override fun logI(tag: Any, message: String) {
        if (level.isMoreOrEqualsThan(ChatLogLevel.ALL)) {
            Log.i(getTag(tag), message)
        }
        handler?.logI(getTag(tag), message)
    }

    override fun logD(tag: Any, message: String) {
        if (level.isMoreOrEqualsThan(ChatLogLevel.DEBUG)) {
            Log.d(getTag(tag), message)
        }
        handler?.logD(getTag(tag), message)
    }

    override fun logW(tag: Any, message: String) {
        if (level.isMoreOrEqualsThan(ChatLogLevel.WARN)) {
            Log.w(getTag(tag), message)
        }
        handler?.logW(getTag(tag), message)
    }

    override fun logE(tag: Any, message: String) {
        if (level.isMoreOrEqualsThan(ChatLogLevel.ERROR)) {
            Log.e(getTag(tag), message)
        }
        handler?.logE(getTag(tag), message)
    }

    override fun getLevel(): ChatLogLevel {
        return level
    }

    private fun getTag(tag: Any?): String {
        val stringTag: String = when (tag) {
            null -> "null"
            is String -> tag
            else -> tag.javaClass.simpleName
        }

        return TAG_PREFIX + stringTag
    }

    private fun getStackString(t: Throwable): String {
        val errors = StringWriter()
        t.printStackTrace(PrintWriter(errors))
        return errors.toString()
    }
}
