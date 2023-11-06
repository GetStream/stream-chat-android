package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.debugger.ChatClientDebugger
import io.getstream.chat.android.client.debugger.SendMessageDebugger
import io.getstream.chat.android.models.Message
import io.getstream.result.Error

import io.getstream.result.Result

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/debugging/">Debugging</a>
 */
class Debugging {

    fun addClientDebugger(context: Context) {
        val client = ChatClient.Builder("apiKey", context)
            .clientDebugger(object : ChatClientDebugger {
                override fun onNonFatalErrorOccurred(tag: String, src: String, desc: String, error: Error) {
                    // TODO: Implement your custom logic here
                }

                override fun debugSendMessage(
                    channelType: String,
                    channelId: String,
                    message: Message,
                    isRetrying: Boolean,
                ): SendMessageDebugger = this@Debugging.debugSendMessage(
                    channelType,
                    channelId,
                    message,
                    isRetrying
                )
            })
            .build()
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/debugging/#debug-message-sending">Debug Message Sending</a>
     */
    private fun debugSendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
    ): SendMessageDebugger = object : SendMessageDebugger {
        override fun onStart(message: Message) {
            // handle onStart
        }

        override fun onInterceptionStart(message: Message) {
            // handle onInterceptionStart
        }

        override fun onInterceptionUpdate(message: Message) {
            // handle onInterceptionUpdate
        }

        override fun onInterceptionStop(result: Result<Message>, message: Message) {
            // handle onInterceptionStop
        }

        override fun onSendStart(message: Message) {
            // handle onSendStart
        }

        override fun onSendStop(result: Result<Message>, message: Message) {
            // handle onSendStop
        }

        override fun onStop(result: Result<Message>, message: Message) {
            // handle onStop
        }
    }
}