package io.getstream.chat.android.client.logger

import android.util.Log

class ChatLoggerImpl constructor(
    private val loggingLevel: ChatLogLevel = ChatLogLevel.NOTHING,
    private val loggingHandler: ChatLoggerHandler? = null
) : ChatLogger {

    override fun logE(tag: Any, message: String, throwable: Throwable) {
        if (loggingLevel.isMoreOrEqualsThan(ChatLogLevel.ERROR)) {
            throwable.printStackTrace()
            Log.e(getTag(tag), message)
        }
        loggingHandler?.logE(tag, message, throwable)
    }

    override fun logE(tag: Any, throwable: Throwable) {
        if (loggingLevel.isMoreOrEqualsThan(ChatLogLevel.ERROR)) {
            throwable.printStackTrace()
        }
        loggingHandler?.logT(getTag(tag), throwable)
    }

    override fun logI(tag: Any, message: String) {
        if (loggingLevel.isMoreOrEqualsThan(ChatLogLevel.ALL)) {
            Log.i(getTag(tag), message)
        }
        loggingHandler?.logI(getTag(tag), message)
    }

    override fun logD(tag: Any, message: String) {
        if (loggingLevel.isMoreOrEqualsThan(ChatLogLevel.DEBUG)) {
            Log.d(getTag(tag), message)
        }
        loggingHandler?.logD(getTag(tag), message)
    }

    override fun logW(tag: Any, message: String) {
        if (loggingLevel.isMoreOrEqualsThan(ChatLogLevel.WARN)) {
            Log.w(getTag(tag), message)
        }
        loggingHandler?.logW(getTag(tag), message)
    }

    override fun logE(tag: Any, message: String) {
        if (loggingLevel.isMoreOrEqualsThan(ChatLogLevel.ERROR)) {
            Log.e(getTag(tag), message)
        }
        loggingHandler?.logE(getTag(tag), message)
    }

    private fun getTag(tag: Any?): String {
        if (tag == null) return "null"
        return if (tag is String) {
            tag
        } else {
            tag.javaClass.simpleName
        }
    }
}