@file:Suppress("unused")

package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerHandler

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/logging/">Logging</a>
 */
class Logging(private val context: Context) {

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/logging/#intercepting-logs">Intercepting Logs</a>
     */
    fun addLoggingHandler() {
        val client = ChatClient.Builder("apiKey", context)
            .logLevel(ChatLogLevel.ALL)
            .loggerHandler(object : ChatLoggerHandler {
                override fun logT(throwable: Throwable) {
                    // custom logging
                }

                override fun logT(tag: Any, throwable: Throwable) {
                    // custom logging
                }

                override fun logI(tag: Any, message: String) {
                    // custom logging
                }

                override fun logD(tag: Any, message: String) {
                    // custom logging
                }

                override fun logW(tag: Any, message: String) {
                    // custom logging
                }

                override fun logE(tag: Any, message: String) {
                    // custom logging
                }

                override fun logE(tag: Any, message: String, throwable: Throwable) {
                    // custom logging
                }
            })
            .build()
    }
}
