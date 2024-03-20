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

package io.getstream.chat.ui.sample.feature.chat.messagelist.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrNull
import io.getstream.log.taggedLogger
import io.getstream.result.call.Call
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageDetailsViewModel(
    private val cid: String,
    private val messageId: String,
    private val chatClient: ChatClient,
) : ViewModel() {

    private val logger by taggedLogger("MessageDetails-VM")

    private val channel by lazy { chatClient.channel(cid) }

    val _state = MutableStateFlow<MessageDetailsViewState>(MessageDetailsViewState.Empty)
    val state: StateFlow<MessageDetailsViewState> = _state

    init {
        iniState()
    }

    private fun iniState() {
        viewModelScope.launch {
            _state.value = MessageDetailsViewState.Loading
            try {
                val deferredChannel = async { channel.getChannelState().await() }
                val deferredMessage = async { channel.getMessage(messageId).await() }
                val channel = deferredChannel.await().getOrThrow()
                val message = deferredMessage.await().getOrThrow()

                val sentBy = message.user.name
                val createdAt = message.getCreatedAtOrNull() ?: error("Message created at is null")
                val readBy = channel.read
                    .filter { it.lastRead > createdAt }

                _state.value = MessageDetailsViewState.Loaded(
                    sentBy = sentBy,
                    createdAt = createdAt.toString(),
                    readBy = readBy,
                )
            } catch (e: Exception) {
                logger.e(e) { "Failed to get message with id: $messageId" }
                _state.value = MessageDetailsViewState.Failed(e.message ?: "Failed to get message: $messageId")
            }
        }
    }
}

sealed class MessageDetailsViewState {
    data object Empty : MessageDetailsViewState()
    data object Loading : MessageDetailsViewState()
    data class Loaded(
        val sentBy: String,
        val createdAt: String,
        val readBy: List<ChannelUserRead>,
    ) : MessageDetailsViewState()
    data class Failed(val error: String) : MessageDetailsViewState()
}

class MessageDetailsViewModelFactory(
    private val cid: String,
    private val messageId: String,
) : ViewModelProvider.Factory {
    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageDetailsViewModel::class.java to { MessageDetailsViewModel(cid, messageId, ChatClient.instance()) },
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException(
                "MessageDetailsViewModelFactory can only create instances " +
                    "of the following classes: ${factories.keys.joinToString { it.simpleName }}",
            )

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}

private fun ChannelClient.getChannelState(): Call<Channel> {
    return query(
        QueryChannelRequest().apply {
            state = true
        },
    )
}
