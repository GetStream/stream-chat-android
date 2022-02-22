package io.getstream.chat.android.client.experimental.errorhandler

import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Extension for [io.getstream.chat.android.client.ChatClient] that allows handling plugins' errors.
 *
 * @see [io.getstream.chat.android.client.experimental.plugin.Plugin]
 */
@ExperimentalStreamChatApi
public interface ErrorHandler : Comparable<ErrorHandler> {

    /**
     * The name of this plugin.
     */
    public val name: String

    /**
     * The priority of this [ErrorHandler]. Use it to run it before error handlers of the same type.
     */
    public val priority: Int

    override fun compareTo(other: ErrorHandler): Int {
        return this.priority.compareTo(other.priority)
    }

    public companion object {

        /**
         * Default priority
         */
        public const val DEFAULT_PRIORITY: Int = 1
    }
}
