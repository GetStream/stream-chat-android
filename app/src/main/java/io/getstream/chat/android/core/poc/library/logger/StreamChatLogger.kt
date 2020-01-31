package io.getstream.chat.android.core.poc.library.logger

import android.util.Log

class StreamChatLogger constructor(
    private val loggingLevel: StreamLoggerLevel? = StreamLoggerLevel.NOTHING,
    private val loggingHandler: StreamLoggerHandler? = null
) : StreamLogger {

    override fun logT(throwable: Throwable) {
        if (loggingLevel?.isMoreOrEqualsThan(StreamLoggerLevel.ERROR) != false) {
            throwable.printStackTrace()
        }
        loggingHandler?.logT(throwable)
    }

    override fun logT(tag: Any, throwable: Throwable) {
        if (loggingLevel?.isMoreOrEqualsThan(StreamLoggerLevel.ERROR) != false) {
            throwable.printStackTrace()
        }
        loggingHandler?.logT(getTag(tag), throwable)
    }

    override fun logI(tag: Any, message: String) {
        if (loggingLevel?.isMoreOrEqualsThan(StreamLoggerLevel.ALL) != false) {
            Log.i(getTag(tag), message)
        }
        loggingHandler?.logI(getTag(tag), message)
    }

    override fun logD(tag: Any, message: String) {
        if (loggingLevel?.isMoreOrEqualsThan(StreamLoggerLevel.DEBUG) != false) {
            Log.d(getTag(tag), message)
        }
        loggingHandler?.logD(getTag(tag), message)
    }

    override fun logW(tag: Any, message: String) {
        if (loggingLevel?.isMoreOrEqualsThan(StreamLoggerLevel.WARN) != false) {
            Log.w(getTag(tag), message)
        }
        loggingHandler?.logW(getTag(tag), message)
    }

    override fun logE(tag: Any, message: String) {
        if (loggingLevel?.isMoreOrEqualsThan(StreamLoggerLevel.ERROR) != false) {
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
        private var loggingLevel: StreamLoggerLevel? = null
        private var loggingHandler: StreamLoggerHandler? = null

        /**
         * Set logging level
         *
         * @param level - Logging [StreamLoggerLevel]
         * @return - builder
         */

        fun loggingLevel(level: StreamLoggerLevel): Builder {
            loggingLevel = level
            return this
        }

        fun setLoggingHandler(handler: StreamLoggerHandler): Builder {
            loggingHandler = handler
            return this
        }

        fun build(): StreamChatLogger {
            return StreamChatLogger(loggingLevel, loggingHandler)
        }
    }
}