/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.debugger

import io.getstream.chat.android.client.debugger.ChatClientDebugger
import io.getstream.chat.android.client.debugger.SendMessageDebugger
import io.getstream.chat.android.models.Message
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result

class CustomChatClientDebugger : ChatClientDebugger {

    override fun onNonFatalErrorOccurred(tag: String, src: String, desc: String, error: Error) {
        // TODO: Implement your custom logic here
    }

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
    channelType: String,
    channelId: String,
    message: Message,
    isRetrying: Boolean,
) : SendMessageDebugger {

    private val logger by taggedLogger("SendMessageDebugger")

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

    override fun onInterceptionStop(result: Result<Message>, message: Message) {
        logger.v { "[onInterceptionStop] #debug; result: $result, message: $message" }
    }

    override fun onSendStart(message: Message) {
        logger.d { "[onSendStart] #debug; message: $message" }
    }

    override fun onSendStop(result: Result<Message>, message: Message) {
        logger.v { "[onSendStop] #debug; result: $result, message: $message" }
    }

    override fun onStop(result: Result<Message>, message: Message) {
        logger.v { "[onStop] #debug; result: $result, message: $message" }
    }
}
