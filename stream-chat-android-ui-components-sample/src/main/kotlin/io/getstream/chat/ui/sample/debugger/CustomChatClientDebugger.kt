package io.getstream.chat.ui.sample.debugger

import io.getstream.chat.android.client.debugger.ChatClientDebugger
import io.getstream.chat.android.client.debugger.SendMessageDebugger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.logging.StreamLog

class CustomChatClientDebugger : ChatClientDebugger {

    override fun debugSendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean,
    ): SendMessageDebugger {
        return CustomSendMessageDebugger(channelType, channelId, message, isRetrying)
    }
}

class CustomSendMessageDebugger(
    private val channelType: String,
    private val channelId: String,
    private val message: Message,
    private val isRetrying: Boolean,
) : SendMessageDebugger {

    private val logger = StreamLog.getLogger("SendMessageDebugger")

    private val cid = "$channelType:$channelId"

    init {
        logger.i { "<init> #debug; isRetrying: $isRetrying, cid: $cid, message: $message" }
    }

    override fun onStart(message: Message) {
        logger.d { "[onStart] #debug; message: $message" }
    }

    override fun onInterceptionStart(message: Message) {
        logger.d { "[onInterceptionStart] #debug; message: $message" }
    }

    override fun onInterceptionUpdate(message: Message) {
        logger.d { "[onInterceptionUpdate] #debug; message: $message" }
    }

    override fun onInterceptionStop(result: Result<Message>) {
        logger.v { "[onInterceptionStop] #debug; result: $result" }
    }

    override fun onSendStart(message: Message) {
        logger.d { "[onSendStart] #debug; message: $message" }
    }

    override fun onSendStop(result: Result<Message>) {
        logger.v { "[onSendStop] #debug; result: $result" }
    }

    override fun onStop(result: Result<Message>) {
        logger.v { "[onStop] #debug; result: $result" }
    }
}