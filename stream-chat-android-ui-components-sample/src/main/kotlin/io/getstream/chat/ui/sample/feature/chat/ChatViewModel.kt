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

package io.getstream.chat.ui.sample.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.utils.Debouncer
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.state.utils.Event
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.model.MessageListItemWrapper
import io.getstream.chat.ui.sample.application.MessageTranslator
import io.getstream.chat.ui.sample.util.extensions.isAnonymousChannel
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ChatViewModel(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private companion object {
        private const val TRANSLATION_DEBOUNCE_MS = 300L
    }

    private val logger by taggedLogger("Chat:ChannelVM")

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: StateFlow<ChannelState?> = observeChannelState()

    private val _navigationEvent: MutableLiveData<Event<NavigationEvent>> = MutableLiveData()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    private val _translationEvent: MutableLiveData<Event<Message>> = MutableLiveData()
    val translationEvent: LiveData<Event<Message>> = _translationEvent

    val members: LiveData<List<Member>> = channelState.filterNotNull().flatMapLatest { it.members }.asLiveData()

    private val translateDebouncer = Debouncer(TRANSLATION_DEBOUNCE_MS, viewModelScope)

    private fun observeChannelState(): StateFlow<ChannelState?> {
        val messageLimit = 0
        logger.d { "[observeChannelState] cid: $cid, messageLimit: $messageLimit" }
        return chatClient.watchChannelAsState(cid, messageLimit, viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        translateDebouncer.cancelLastDebounce()
        MessageTranslator.clearTranslations()
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.HeaderClicked -> {
                val channelData = requireNotNull(channelState.value?.channelData?.value)
                _navigationEvent.value = Event(
                    if (action.members.size > 2 || !channelData.isAnonymousChannel()) {
                        NavigationEvent.NavigateToGroupChatInfo(cid)
                    } else {
                        NavigationEvent.NavigateToChatInfo(cid)
                    },
                )
            }
            is Action.Translate -> {
                translate(action.message)
            }
            is Action.ClearTranslation -> {
                clearTranslation(action.message)
            }
            is Action.DeleteMessageForMe -> {
                chatClient.deleteMessageForMe(messageId = action.message.id)
                    .enqueue()
            }
        }
    }

    fun onMessageListState(state: MessageListItemWrapper) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val messages = state.items.filterIsInstance<MessageListItem.MessageItem>().map { it.message }
                translationEvent.value?.peekContent()?.also { toBeTranslated ->
                    messages.firstOrNull {
                        it.id == toBeTranslated.id && it.i18n.isNotEmpty()
                    }?.also {
                        logger.v { "[onMessageListState] found: ${it.text}, i18n: ${it.i18n}" }
                        emitTranslation(toBeTranslated)
                    }
                }
            }
        }
    }

    private fun clearTranslation(message: Message) {
        logger.d { "[clearTranslation] text: ${message.text}, i18n: ${message.i18n}" }
        MessageTranslator.clearTranslation(message.id)
        _translationEvent.value = Event(message)
    }

    private fun translate(message: Message) {
        viewModelScope.launch {
            val language = chatClient.getCurrentUser()?.language ?: Locale.getDefault().language
            logger.d { "[translate] to: \"$language\", text: ${message.text}, i18n: ${message.i18n}" }
            when (message.getTranslation(language).isNotEmpty()) {
                true -> {
                    logger.v { "[translate] already translated" }
                    MessageTranslator.translate(message.id)
                    _translationEvent.value = Event(message)
                }
                else -> when (val result = chatClient.translate(message.id, language).await()) {
                    is Result.Success -> {
                        val translatedMessage = result.value
                        logger.v { "[translate] succeed: ${translatedMessage.text}, i18n: ${translatedMessage.i18n}" }
                        MessageTranslator.translate(message.id)
                        emitTranslation(translatedMessage)
                    }

                    is Result.Failure -> {
                        logger.e { "[translate] failed: ${result.value}" }
                        MessageTranslator.clearTranslation(message.id)
                        emitTranslation(message)
                    }
                }
            }
        }
    }

    private fun emitTranslation(message: Message) {
        translateDebouncer.submit {
            logger.v { "[emitTranslation] text: ${message.text}, i18n: ${message.i18n}" }
            _translationEvent.value = Event(message)
        }
    }

    sealed class Action {
        class HeaderClicked(val members: List<Member>) : Action()
        class Translate(val message: Message) : Action()
        class ClearTranslation(val message: Message) : Action()
        class DeleteMessageForMe(val message: Message) : Action()
    }

    sealed class NavigationEvent {
        abstract val cid: String

        data class NavigateToChatInfo(override val cid: String) : NavigationEvent()
        data class NavigateToGroupChatInfo(override val cid: String) : NavigationEvent()
    }
}
