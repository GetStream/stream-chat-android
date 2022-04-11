/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.logging

import io.getstream.logging.StreamLog.isLoggable
import io.getstream.logging.StreamLogger.Level.DEBUG
import io.getstream.logging.StreamLogger.Level.ERROR
import io.getstream.logging.StreamLogger.Level.INFO
import io.getstream.logging.StreamLogger.Level.VERBOSE
import io.getstream.logging.StreamLogger.Level.WARN

/**
 * Send a [ERROR] log message.
 *
 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
 * @param throwable An exception to log.
 * @param message The function returning a message you would like logged.
 */
public inline fun logE(tag: String, throwable: Throwable, crossinline message: () -> String) {
    if (isLoggable(ERROR, tag)) {
        StreamLog.e(tag, throwable) { message() }
    }
}

/**
 * Send a [ERROR] log message.
 *
 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
 * @param message The function returning a message you would like logged.
 */
public inline fun logE(tag: String, crossinline message: () -> String) {
    if (isLoggable(ERROR, tag)) {
        StreamLog.e(tag) { message() }
    }
}

/**
 * Send a [WARN] log message.
 *
 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
 * @param message The function returning a message you would like logged.
 */
public inline fun logW(tag: String, crossinline message: () -> String) {
    if (isLoggable(WARN, tag)) {
        StreamLog.w(tag) { message() }
    }
}

/**
 * Send a [INFO] log message.
 *
 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
 * @param message The function returning a message you would like logged.
 */
public inline fun logI(tag: String, crossinline message: () -> String) {
    if (isLoggable(INFO, tag)) {
        StreamLog.i(tag) { message() }
    }
}

/**
 * Send a [DEBUG] log message.
 *
 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
 * @param message The function returning a message you would like logged.
 */
public inline fun logD(tag: String, crossinline message: () -> String) {
    if (isLoggable(DEBUG, tag)) {
        StreamLog.d(tag) { message() }
    }
}

/**
 * Send a [VERBOSE] log message.
 *
 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
 * @param message The function returning a message you would like logged.
 */
public inline fun logV(tag: String, crossinline message: () -> String) {
    if (isLoggable(VERBOSE, tag)) {
        StreamLog.v(tag) { message() }
    }
}

public object StreamLog {

    private var logger: StreamLogger = SilentStreamLogger
    private var isLoggable: (priority: Priority, tag: String) -> Boolean = { _, _ -> false }

    /**
     * Sets custom [StreamLogger] implementation.
     */
    public fun init(
        logger: StreamLogger,
        isLoggable: (priority: Priority, tag: String) -> Boolean = { _, _ -> false }
    ) {
        this.logger = logger
        this.isLoggable = isLoggable
    }

    public fun isLoggable(priority: Priority, tag: String): Boolean {
        return isLoggable.invoke(priority, tag)
    }

    /**
     * Returns a tagged logger.
     *
     * @return [TaggedLogger] Tagged logger.
     */
    public fun getLogger(tag: String): TaggedLogger = TaggedLoggerImpl(tag, logger)

    /**
     * Send a [ERROR] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param throwable An exception to log.
     * @param message The function returning a message you would like logged.
     */
    public fun e(tag: String, throwable: Throwable, message: () -> String) {
        if (isLoggable(ERROR, tag)) {
            logger.log(ERROR, tag, message, throwable)
        }
    }

    /**
     * Send a [ERROR] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun e(tag: String, message: () -> String) {
        if (isLoggable(ERROR, tag)) {
            logger.log(ERROR, tag, message)
        }
    }

    /**
     * Send a [WARN] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun w(tag: String, message: () -> String) {
        if (isLoggable(WARN, tag)) {
            logger.log(WARN, tag, message)
        }
    }

    /**
     * Send a [INFO] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun i(tag: String, message: () -> String) {
        if (isLoggable(INFO, tag)) {
            logger.log(INFO, tag, message)
        }
    }

    /**
     * Send a [DEBUG] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun d(tag: String, message: () -> String) {
        if (isLoggable(DEBUG, tag)) {
            logger.log(DEBUG, tag, message)
        }
    }

    /**
     * Send a [VERBOSE] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun v(tag: String, message: () -> String) {
        if (isLoggable(VERBOSE, tag)) {
            logger.log(VERBOSE, tag, message)
        }
    }
}

/**
 * Represents a tagged logger.
 */
public interface TaggedLogger {

    /**
     * Send a [ERROR] log message.
     *
     * @param throwable An exception to log.
     * @param message The function returning a message you would like logged.
     */
    public fun e(throwable: Throwable, message: () -> String)

    /**
     * Send a [ERROR] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun e(message: () -> String)

    /**
     * Send a [WARN] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun w(message: () -> String)

    /**
     * Send a [INFO] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun i(message: () -> String)

    /**
     * Send a [DEBUG] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun d(message: () -> String)

    /**
     * Send a [VERBOSE] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun v(message: () -> String)
}
