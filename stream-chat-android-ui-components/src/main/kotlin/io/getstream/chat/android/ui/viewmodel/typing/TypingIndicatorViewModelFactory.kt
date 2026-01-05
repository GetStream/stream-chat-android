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

package io.getstream.chat.android.ui.viewmodel.typing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient

/**
 * A ViewModel factory for TypingIndicatorViewModel.
 *
 * @param cid The channel id in the format messaging:123.
 * @param chatClient The [ChatClient] instance.
 * @param messageId The id of the message we wish to focus to. Used to limit the number of channel queries as well.
 *
 * @see TypingIndicatorViewModel
 */
public class TypingIndicatorViewModelFactory(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val messageId: String? = null,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == TypingIndicatorViewModel::class.java) {
            "TypingIndicatorViewModelFactory can only create instances of TypingIndicatorViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return TypingIndicatorViewModel(cid, chatClient, messageId) as T
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public class Builder
    @SinceKotlin("99999.9")
    constructor() {
        private var cid: String? = null
        private var chatClient: ChatClient? = null
        private var messageId: String? = null

        /**
         * Sets the channel id in the format messaging:123.
         */
        public fun cid(cid: String): Builder = apply {
            this.cid = cid
        }

        /**
         * Sets the [ChatClient] instance.
         */
        public fun chatClient(chatClient: ChatClient): Builder = apply {
            this.chatClient = chatClient
        }

        /**
         * Sets the messageId of the [Message] we wish to scroll to.
         */
        public fun messageId(messageId: String?): Builder = apply {
            this.messageId = messageId
        }

        /**
         * Builds [TypingIndicatorViewModelFactory] instance.
         */
        public fun build(): ViewModelProvider.Factory {
            return TypingIndicatorViewModelFactory(
                cid = cid ?: error("Channel cid should not be null"),
                chatClient = chatClient ?: ChatClient.instance(),
                messageId = messageId,
            )
        }
    }
}
