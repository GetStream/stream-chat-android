/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.e2e.test.robots

import io.getstream.chat.android.compose.uiautomator.defaultTimeout
import io.getstream.chat.android.compose.uiautomator.mockServer
import io.getstream.chat.android.e2e.test.mockserver.AttachmentType
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

public class ParticipantRobot {

    public companion object {
        public const val name: String = "Han Solo"
    }

    public fun startTyping(): ParticipantRobot {
        mockServer.postRequest("participant/typing/start")
        return this
    }

    public fun startTypingInThread(): ParticipantRobot {
        mockServer.postRequest("participant/typing/start?thread=true")
        return this
    }

    public fun stopTyping(): ParticipantRobot {
        mockServer.postRequest("participant/typing/stop")
        return this
    }

    public fun stopTypingInThread(): ParticipantRobot {
        mockServer.postRequest("participant/typing/stop?thread=true")
        return this
    }

    public fun sleep(timeOutMillis: Long = defaultTimeout): ParticipantRobot {
        io.getstream.chat.android.compose.uiautomator.sleep(timeOutMillis)
        return this
    }

    public fun readMessage(parentId: String? = null): ParticipantRobot {
        mockServer.postRequest("participant/read")
        return this
    }

    public fun sendMessage(text: String): ParticipantRobot {
        mockServer.postRequest("participant/message", text.toRequestBody("text".toMediaTypeOrNull()))
        return this
    }

    public fun sendMessageInThread(text: String, alsoSendInChannel: Boolean = false): ParticipantRobot {
        mockServer.postRequest(
            "participant/message?thread=true&thread_and_channel=$alsoSendInChannel",
            text.toRequestBody("text".toMediaTypeOrNull()),
        )
        return this
    }

    public fun editMessage(text: String): ParticipantRobot {
        mockServer.postRequest(
            "participant/message?action=edit",
            text.toRequestBody("text".toMediaTypeOrNull()),
        )
        return this
    }

    public fun deleteMessage(hard: Boolean = false): ParticipantRobot {
        mockServer.postRequest("participant/message?action=delete&hard_delete=$hard")
        return this
    }

    public fun quoteMessage(text: String, last: Boolean = true): ParticipantRobot {
        val quote = if (last) "quote_last=true" else "quote_first=true"
        mockServer.postRequest("participant/message?$quote", text.toRequestBody("text".toMediaTypeOrNull()))
        return this
    }

    public fun quoteMessageInThread(
        text: String,
        alsoSendInChannel: Boolean = false,
        last: Boolean = true,
    ): ParticipantRobot {
        val quote = if (last) "quote_last=true" else "quote_first=true"
        mockServer.postRequest(
            "participant/message?$quote&thread=true&thread_and_channel=$alsoSendInChannel",
            text.toRequestBody("text".toMediaTypeOrNull()),
        )
        return this
    }

    public fun uploadGiphy(): ParticipantRobot {
        mockServer.postRequest("participant/message?giphy=true")
        return this
    }

    public fun uploadGiphyInThread(): ParticipantRobot {
        mockServer.postRequest("participant/message?giphy=true&thread=true")
        return this
    }

    public fun quoteMessageWithGiphy(last: Boolean = true): ParticipantRobot {
        val quote = if (last) "quote_last=true" else "quote_first=true"
        mockServer.postRequest("participant/message?giphy=true&$quote")
        return this
    }

    public fun quoteMessageWithGiphyInThread(
        alsoSendInChannel: Boolean = false,
        last: Boolean = true,
    ): ParticipantRobot {
        val quote = if (last) "quote_last=true" else "quote_first=true"
        val endpoint = "participant/message?giphy=true&$quote&thread=true&thread_and_channel=$alsoSendInChannel"
        mockServer.postRequest(endpoint)
        return this
    }

    public fun pinMesage(): ParticipantRobot {
        mockServer.postRequest("participant/message?action=pin")
        return this
    }

    public fun unpinMesage(): ParticipantRobot {
        mockServer.postRequest("participant/message?action=unpin")
        return this
    }

    public fun uploadAttachment(type: AttachmentType, count: Int = 1): ParticipantRobot {
        mockServer.postRequest("participant/message?$type=$count")
        return this
    }

    public fun quoteMessageWithAttachment(
        type: AttachmentType,
        count: Int = 1,
        last: Boolean = true,
    ): ParticipantRobot {
        val quote = if (last) "quote_last=true" else "quote_first=true"
        mockServer.postRequest("participant/message?$quote&$type=$count")
        return this
    }

    public fun uploadAttachmentInThread(
        type: AttachmentType,
        count: Int = 1,
        alsoSendInChannel: Boolean = false,
    ): ParticipantRobot {
        val endpoint = "participant/message?$type=$count&thread=true&thread_and_channel=$alsoSendInChannel"
        mockServer.postRequest(endpoint)
        return this
    }

    public fun quoteMessageWithAttachmentInThread(
        type: AttachmentType,
        count: Int = 1,
        alsoSendInChannel: Boolean = false,
        last: Boolean = true,
    ): ParticipantRobot {
        val quote = if (last) "quote_last=true" else "quote_first=true"
        val endpoint = "participant/message?$quote&$type=$count&thread=true&thread_and_channel=$alsoSendInChannel"
        mockServer.postRequest(endpoint)
        return this
    }

    public fun addReaction(type: ReactionType): ParticipantRobot {
        mockServer.postRequest("participant/reaction?type=${type.reaction}")
        return this
    }

    public fun deleteReaction(type: ReactionType): ParticipantRobot {
        mockServer.postRequest("participant/reaction?type=${type.reaction}&delete=true")
        return this
    }
}
