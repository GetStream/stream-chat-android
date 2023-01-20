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

package io.getstream.chat.android.ui.message.list.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.common.composer.MessageComposerController
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel

/**
 * A ViewModel factory for MessageListViewModel, MessageListHeaderViewModel and MessageInputViewModel.
 *
 * @param cid The channel id in the format messaging:123.
 * @param messageId The id of the target message to displayed.
 * @param messageLimit The message limit when loading a new page.
 *
 * @see MessageListViewModel
 * @see MessageListHeaderViewModel
 * @see MessageInputViewModel
 */
public class MessageListViewModelFactory @JvmOverloads constructor(
    private val cid: String,
    private val messageId: String? = null,
    private val messageLimit: Int = MessageListViewModel.DEFAULT_MESSAGES_LIMIT,
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageListHeaderViewModel::class.java to { MessageListHeaderViewModel(cid) },
        MessageInputViewModel::class.java to { MessageInputViewModel(cid) },
        MessageListViewModel::class.java to {
            MessageListViewModel(
                cid = cid,
                messageId = messageId,
                messageLimit = messageLimit
            )
        },
        MessageComposerViewModel::class.java to { MessageComposerViewModel(MessageComposerController(cid)) },
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("MessageListViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public class Builder @SinceKotlin("99999.9") constructor() {
        private var cid: String? = null
        private var messageId: String? = null
        private var messageLimit: Int = MessageListViewModel.DEFAULT_MESSAGES_LIMIT

        /**
         * Sets the channel id in the format messaging:123.
         */
        public fun cid(cid: String): Builder = apply {
            this.cid = cid
        }

        /**
         * Sets the id of the target message to displayed.
         */
        public fun messageId(messageId: String): Builder = apply {
            this.messageId = messageId
        }

        /**
         * Sets the number of messages that are loaded when loading a new page.
         */
        public fun messageLimit(messageLimit: Int): Builder = apply {
            this.messageLimit = messageLimit
        }

        /**
         * Builds [MessageListViewModelFactory] instance.
         */
        public fun build(): ViewModelProvider.Factory {
            return MessageListViewModelFactory(
                cid = cid ?: error("Channel cid should not be null"),
                messageId = messageId,
                messageLimit = messageLimit
            )
        }
    }
}
