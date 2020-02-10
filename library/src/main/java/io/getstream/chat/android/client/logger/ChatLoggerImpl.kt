package io.getstream.chat.android.client.logger

import android.util.Log

class ChatLoggerImpl constructor(
    private val loggingLevel: ChatLoggerLevel? = ChatLoggerLevel.NOTHING,
    private val loggingHandler: ChatLoggerHandler? = null
) : ChatLogger {

    override fun logT(throwable: Throwable) {
        if (loggingLevel?.isMoreOrEqualsThan(ChatLoggerLevel.ERROR) != false) {
            throwable.printStackTrace()
        }
        loggingHandler?.logT(throwable)
    }

    override fun logT(tag: Any, throwable: Throwable) {
        if (loggingLevel?.isMoreOrEqualsThan(ChatLoggerLevel.ERROR) != false) {
            throwable.printStackTrace()
        }
        loggingHandler?.logT(getTag(tag), throwable)
    }

    override fun logI(tag: Any, message: String) {
        if (loggingLevel?.isMoreOrEqualsThan(ChatLoggerLevel.ALL) != false) {
            Log.i(getTag(tag), message)
        }
        loggingHandler?.logI(getTag(tag), message)
    }

    override fun logD(tag: Any, message: String) {
        if (loggingLevel?.isMoreOrEqualsThan(ChatLoggerLevel.DEBUG) != false) {
            Log.d(getTag(tag), message)
        }
        loggingHandler?.logD(getTag(tag), message)
    }

    override fun logW(tag: Any, message: String) {
        if (loggingLevel?.isMoreOrEqualsThan(ChatLoggerLevel.WARN) != false) {
            Log.w(getTag(tag), message)
        }
        loggingHandler?.logW(getTag(tag), message)
    }

    override fun logE(tag: Any, message: String) {
        if (loggingLevel?.isMoreOrEqualsThan(ChatLoggerLevel.ERROR) != false) {
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

    class Builder {
        private var loggingLevel: ChatLoggerLevel? = null
        private var loggingHandler: ChatLoggerHandler? = null

        /**
         * Set logging level
         *
         * @param level - Logging [ChatLoggerLevel]
         * @return - builder
         */

        fun level(level: ChatLoggerLevel): Builder {
            loggingLevel = level
            return this
        }

        fun handler(handler: ChatLoggerHandler): Builder {
            loggingHandler = handler
            return this
        }

        fun build(): ChatLoggerImpl {
            return ChatLoggerImpl(loggingLevel, loggingHandler)
        }
    }
}