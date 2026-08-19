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

package io.getstream.chat.android.e2e.test.robots

import io.getstream.chat.android.e2e.test.mockserver.AttachmentType
import io.getstream.chat.android.e2e.test.mockserver.MockServer
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder

public class ParticipantRobot(
    private val mockServer: MockServer,
) {

    public companion object {
        public const val name: String = "Count Dooku"
        public const val id: String = "count_dooku"
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

    public fun readMessage(): ParticipantRobot {
        mockServer.postRequest("participant/read")
        return this
    }

    /**
     * Delivers a push notification for the last message to the Android app under test.
     *
     * @param component The broadcast receiver component of the app under test.
     * @param rest Optional payload degradation, matching the mock server's `rest` values.
     */
    public fun sendPushNotification(component: String, rest: String? = null): ParticipantRobot {
        var endpoint = "participant/push?platform=android&component=$component"
        if (rest != null) {
            endpoint += "&rest=$rest"
        }
        mockServer.postRequest(endpoint)
        return this
    }

    public fun sendMessage(text: String, delay: Int = 0): ParticipantRobot {
        var endpoint = "participant/message"
        if (delay > 0) {
            endpoint += "?delay=$delay"
        }
        mockServer.postRequest(endpoint, text.toRequestBody("text".toMediaTypeOrNull()))
        return this
    }

    /**
     * Sends [count] messages named `"$text-1"` through `"$text-$count"`, spaced 300ms apart.
     *
     * @param text The base text of every message; the one-based index is appended after a dash.
     * @param count How many messages to send.
     */
    public fun sendMultipleMessages(text: String, count: Int): ParticipantRobot {
        repeat(count) { index ->
            sendMessage("$text-${index + 1}")
            Thread.sleep(MULTIPLE_MESSAGES_INTERVAL_MILLIS)
        }
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

    public fun pinMessage(): ParticipantRobot {
        mockServer.postRequest("participant/message?action=pin")
        return this
    }

    public fun unpinMessage(): ParticipantRobot {
        mockServer.postRequest("participant/message?action=unpin")
        return this
    }

    public fun uploadAttachment(type: AttachmentType, count: Int = 1): ParticipantRobot {
        mockServer.postRequest("participant/message?${type.attachment}=$count")
        return this
    }

    public fun quoteMessageWithAttachment(
        type: AttachmentType,
        count: Int = 1,
        last: Boolean = true,
    ): ParticipantRobot {
        val quote = if (last) "quote_last=true" else "quote_first=true"
        mockServer.postRequest("participant/message?$quote&${type.attachment}=$count")
        return this
    }

    public fun uploadAttachmentInThread(
        type: AttachmentType,
        count: Int = 1,
        alsoSendInChannel: Boolean = false,
    ): ParticipantRobot {
        val endpoint = "participant/message?${type.attachment}=$count&thread=true&thread_and_channel=$alsoSendInChannel"
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
        val endpoint = "participant/message?" +
            "$quote&${type.attachment}=$count&thread=true&thread_and_channel=$alsoSendInChannel"
        mockServer.postRequest(endpoint)
        return this
    }

    /**
     * Casts a vote in the poll of the newest poll message.
     *
     * @param option The text of the poll option to vote for.
     */
    public fun castPollVote(option: String): ParticipantRobot {
        mockServer.postRequest("participant/poll_vote?option=${option.urlEncoded()}")
        return this
    }

    /**
     * Adds an answer (comment) to the poll of the newest poll message.
     *
     * @param answer The answer text.
     */
    public fun addPollAnswer(answer: String): ParticipantRobot {
        mockServer.postRequest("participant/poll_vote?answer=${answer.urlEncoded()}")
        return this
    }

    private fun String.urlEncoded(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

    public fun addReaction(type: ReactionType, delay: Int = 0): ParticipantRobot {
        var endpoint = "participant/reaction?type=${type.reaction}"
        if (delay > 0) {
            endpoint += "&delay=$delay"
        }
        mockServer.postRequest(endpoint)
        return this
    }

    public fun deleteReaction(type: ReactionType): ParticipantRobot {
        mockServer.postRequest("participant/reaction?type=${type.reaction}&delete=true")
        return this
    }
}

private const val MULTIPLE_MESSAGES_INTERVAL_MILLIS = 300L
