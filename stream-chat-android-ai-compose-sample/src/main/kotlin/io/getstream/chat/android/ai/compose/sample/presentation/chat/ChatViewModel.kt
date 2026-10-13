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

package io.getstream.chat.android.ai.compose.sample.presentation.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.sample.data.repository.ChatAiRepository
import io.getstream.chat.android.ai.compose.sample.domain.isFromAi
import io.getstream.chat.android.ai.compose.ui.component.MessageData
import io.getstream.chat.android.ai.ondevice.AIConversationTurn
import io.getstream.chat.android.ai.ondevice.AIOnDeviceModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.watchChannelAsState
import io.getstream.chat.android.client.channel.subscribeFor
import io.getstream.chat.android.client.events.AIIndicatorClearEvent
import io.getstream.chat.android.client.events.AIIndicatorStopEvent
import io.getstream.chat.android.client.events.AIIndicatorUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import io.getstream.chat.android.models.Message as StreamMessage

/**
 * ViewModel for managing chat conversation state and interactions.
 * Handles message sending, AI agent management, and UI state updates.
 *
 * @param chatClient The Stream Chat client instance
 * @param chatAiRepository Repository for Chat AI operations
 * @param conversationId Optional conversation ID. If null, a new conversation will be created on first message.
 */
@OptIn(ExperimentalCoroutinesApi::class, InternalStreamChatApi::class)
internal class ChatViewModel(
    private val chatClient: ChatClient,
    private val chatAiRepository: ChatAiRepository,
    appContext: Context,
    conversationId: String?,
) : ViewModel() {

    private val attachmentStorageHelper = AttachmentStorageHelper(appContext)

    // Answers when the AI agent couldn't be started, such as without a connection to the backend.
    private val onDeviceModel = AIOnDeviceModel(appContext)
    private var agentFailed = false
    private var onDeviceAnswer: Job? = null

    // Questions answered on the device and their answers, newest first. They are not sent to Stream.
    private val localMessages = MutableStateFlow<List<ChatUiState.Message>>(emptyList())

    private val logger by taggedLogger()

    private val cid = MutableStateFlow(conversationId)

    private val _uiState = MutableStateFlow(ChatUiState())

    /**
     * The current UI state of the chat conversation.
     * Observing this StateFlow allows the UI to reactively update when the state changes.
     */
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val currentUserId = chatClient.clientState.user
        .mapNotNull { user -> user?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "",
        )

    // Message to be sent once the AI agent is started
    private var pendingMessage: StreamMessage? = null

    init {
        cid.filterNotNull()
            .onEach { _uiState.update { state -> state.copy(isLoading = state.messages.isEmpty()) } }
            // Start the AI agent
            .onEach(::startAIAgentForChannel)
            // Subscribe to channel events
            .onEach { cid ->
                logger.d { "Subscribing to chat events for channel: $cid" }
                chatClient.channel(cid).subscribeFor<ChatEvent>(::handleChatEvent)
            }
            // Observe messages in the channel
            .flatMapLatest { cid ->
                logger.d { "Watching channel for messages: $cid" }
                chatClient.watchChannelAsState(cid = cid, messageLimit = 30)
            }
            .filterNotNull()
            .flatMapLatest { channelState ->
                combine(channelState.channelData, channelState.messages) {
                    channelState.toChannel()
                }
            }
            .combine(localMessages) { channel, local -> channel to local }
            .onEach { (channel, local) ->
                val title = channel.name.takeIf(String::isNotBlank) ?: "New Chat"

                val messages = channel.messages
                    .mapNotNull { message -> message.toChatMessage(currentUserId.value) }
                    .reversed()

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        title = title,
                        actions = buildList {
                            if (cid.value != null) {
                                add(ChatUiState.Action.NewChat)
                                if (channel.ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)) {
                                    add(ChatUiState.Action.DeleteChat)
                                }
                            }
                        },
                        messages = local + messages,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Sends a message via Stream Chat.
     *
     * Validates input, builds lightweight attachments off the main thread, optimistically
     * updates the UI, then resolves attachment files before dispatching to the SDK.
     * When no channel exists yet, one is created first and the message is queued until the
     * AI agent is ready.
     */
    fun sendMessage(data: MessageData) {
        val text = data.text.trim()
        if (text.isEmpty() || _uiState.value.assistantState.isBusy()) {
            return
        }
        // Busy from the tap itself, so a second tap during the IO hop below is ignored.
        _uiState.update { it.copy(assistantState = ChatUiState.AssistantState.Thinking) }

        viewModelScope.launch {
            if (cid.value != null && agentFailed && onDeviceModel.isAvailable()) {
                onDeviceAnswer = launch { answerOnDevice(text) }
                return@launch
            }

            val uris = data.attachments.toList()
            val lightweightAttachments = withContext(Dispatchers.IO) {
                val metadata = attachmentStorageHelper.resolveMetadata(uris)
                attachmentStorageHelper.toAttachments(metadata)
            }

            val message = StreamMessage(
                text = text,
                user = User(id = currentUserId.value),
                attachments = lightweightAttachments,
            )

            _uiState.update { state ->
                state.copy(
                    messages = listOfNotNull(message.toChatMessage(currentUserId.value)) + state.messages,
                )
            }

            // Copies picker URIs into local cache files. Suspend; dispatches IO internally.
            val readyMessage = message.copy(
                attachments = attachmentStorageHelper.resolveAttachmentFiles(message.attachments),
            )

            val cid = cid.value

            if (cid == null) {
                // Queue the message to be sent once the new channel and AI agent are ready.
                pendingMessage = readyMessage

                val memberIds = listOf(currentUserId.value)
                chatClient.createChannel(
                    channelType = "messaging",
                    channelId = UUID.randomUUID().toString(),
                    memberIds = memberIds,
                    extraData = emptyMap(),
                ).enqueue { result ->
                    result.onSuccess { newChannel ->
                        val newCid = newChannel.cid
                        this@ChatViewModel.cid.value = newCid
                        logger.d { "Created new channel with cid: $newCid" }
                    }.onError { e ->
                        logger.e { "Failed to create channel: ${e.message}" }
                    }
                }
            } else {
                sendMessage(cid, readyMessage)
            }
        }
    }

    private suspend fun startAIAgentForChannel(cid: String): Result<Unit> {
        val (channelType, channelId) = cid.cidToTypeAndId()
        val platform = "openai"
        logger.d { "Starting AI agent on channel: $cid, platform: $platform" }

        return chatAiRepository.startAIAgent(
            channelType = channelType,
            channelId = channelId,
            platform = platform,
        ).onSuccess {
            agentFailed = false
            logger.d { "AI agent started successfully on channel: $cid" }
            // Send any pending message that was queued while starting the AI agent
            pendingMessage?.let { message ->
                sendMessage(cid, message) {
                    // Remove from pending once sent
                    pendingMessage = message
                    // Summarize after the first message is sent
                    viewModelScope.launch {
                        val platform = "openai"
                        logger.d { "Summarizing message: ${message.text}" }
                        chatAiRepository.summarize(
                            text = message.text,
                            platform = platform,
                        ).onSuccess {
                            logger.d { "Message summarized successfully" }
                            chatClient.channel(cid).updatePartial(
                                set = mapOf("name" to it),
                            ).enqueue { result ->
                                result.onSuccess {
                                    logger.d { "Channel updated with summary successfully" }
                                }.onError { e ->
                                    logger.e { "Failed to update channel with summary: ${e.message}" }
                                }
                            }
                        }.onFailure { e ->
                            logger.e { "Failed to summarize message: ${e.message}" }
                        }
                    }
                }
            }
        }.onFailure { e ->
            agentFailed = true
            logger.e { "Failed to start AI agent: ${e.message}" }
        }
    }

    /** Answers [question] with the model on the device. The question and answer stay on this screen. */
    private suspend fun answerOnDevice(question: String) {
        val turns = _uiState.value.messages.reversed().mapNotNull(ChatUiState.Message::toTurn) +
            AIConversationTurn.user(question)
        val answerId = UUID.randomUUID().toString()
        localMessages.update { listOf(localMessage(ChatUiState.Message.Role.User, question)) + it }
        _uiState.update { it.copy(assistantState = ChatUiState.AssistantState.Thinking) }

        var finalState: ChatUiState.AssistantState = ChatUiState.AssistantState.Idle
        try {
            onDeviceModel.reply(OnDeviceInstructions, turns).collect { answer ->
                val message = localMessage(ChatUiState.Message.Role.Assistant, answer, answerId, isGenerating = true)
                localMessages.update { messages -> listOf(message) + messages.filterNot { it.id == answerId } }
                _uiState.update { it.copy(assistantState = ChatUiState.AssistantState.Generating) }
            }
        } catch (e: AIOnDeviceModel.Unavailable) {
            logger.e { "The on-device model is unavailable: ${e.message}" }
            finalState = ChatUiState.AssistantState.Error
        } catch (e: AIOnDeviceModel.Failure) {
            logger.e { "The on-device model failed: ${e.cause?.message}" }
            finalState = ChatUiState.AssistantState.Error
        } finally {
            localMessages.update { messages ->
                messages.map { if (it.id == answerId) it.copy(isGenerating = false) else it }
            }
            _uiState.update { it.copy(assistantState = finalState) }
        }
    }

    private fun sendMessage(
        cid: String,
        message: StreamMessage,
        onSuccess: () -> Unit = {},
    ) {
        chatClient.channel(cid)
            .sendMessage(message = message)
            .enqueue { result ->
                result.onSuccess {
                    onSuccess()
                }.onError { e ->
                    logger.e { "Failed to send message: ${e.message}" }
                }
            }
    }

    /**
     * Stops the current streaming response by sending an AI typing indicator stop event to the server.
     * This tells the AI agent to stop generating content for the current message.
     */
    fun stopStreaming() {
        onDeviceAnswer?.cancel()
        val cid = cid.value ?: run {
            logger.d { "No channel available to stop streaming" }
            return
        }

        chatClient.channel(cid)
            .sendEvent(EventType.AI_TYPING_INDICATOR_STOP)
            .enqueue { result ->
                result.onSuccess {
                    logger.d { "Successfully sent AI typing indicator stop event" }
                }.onError { e ->
                    logger.e { "Failed to stop streaming: ${e.message}" }
                }
            }
    }

    private fun handleChatEvent(event: ChatEvent) {
        logger.d { "Received chat event: $event" }

        when (event) {
            is AIIndicatorUpdatedEvent -> {
                handleAIIndicatorUpdated(event)
            }

            is AIIndicatorClearEvent, is AIIndicatorStopEvent -> {
                _uiState.update { state ->
                    state.copy(
                        assistantState = ChatUiState.AssistantState.Idle,
                    )
                }
            }

            else -> Unit
        }
    }

    private fun handleAIIndicatorUpdated(event: AIIndicatorUpdatedEvent) {
        logger.d { "Processing $event" }

        _uiState.update { state ->
            state.copy(
                assistantState = event.aiState.toAssistantState(),
            )
        }
    }

    /**
     * Deletes the current chat channel.
     * Stops the AI agent first, then deletes the channel.
     * Calls the provided callbacks to indicate success or failure.
     *
     * @param onSuccess Callback invoked when the channel is successfully deleted
     * @param onError Callback invoked when deletion fails, with the error details
     */
    fun deleteChannel(
        onSuccess: () -> Unit = {},
        onError: (Error) -> Unit = {},
    ) {
        val cid = cid.value ?: run {
            logger.d { "No channel to delete" }
            return
        }

        viewModelScope.launch {
            // Stop the AI agent first
            stopAIAgent(cid) {
                // Then delete the channel
                chatClient.channel(cid)
                    .delete()
                    .enqueue { result ->
                        result.onSuccess {
                            logger.d { "Channel deleted successfully: $cid" }
                            onSuccess()
                        }.onError { e ->
                            logger.e { "Failed to delete channel: ${e.message}" }
                            onError(e)
                        }
                    }
            }
        }
    }

    override fun onCleared() {
        cid.value?.let(::stopAIAgent)
        onDeviceModel.close()
    }

    private fun stopAIAgent(cid: String, onSuccess: () -> Unit = {}) {
        logger.d { "Stopping AI agent on channel: $cid" }
        MainScope().launch {
            chatAiRepository.stopAIAgent(cid)
                .onSuccess {
                    logger.d { "AI agent stopped successfully on channel: $cid" }
                    onSuccess()
                }
                .onFailure { e ->
                    logger.e { "Failed to stop AI agent: ${e.message}" }
                }
        }
    }
}

private fun StreamMessage.toChatMessage(currentUserId: String): ChatUiState.Message? {
    // Stream lifts `name` out of extraData, and a tool call's step needs it back.
    val parts = AIMessagePart.parts(attachments.map { it.type.orEmpty() to it.extraData + ("name" to it.name) })
    if (text.isBlank() && parts.isEmpty()) {
        return null
    }

    val isFromCurrentUser = user.id == currentUserId
    val role = when {
        isFromAi() -> ChatUiState.Message.Role.Assistant
        isFromCurrentUser -> ChatUiState.Message.Role.User
        else -> ChatUiState.Message.Role.Other
    }

    return ChatUiState.Message(
        id = id,
        role = role,
        content = text,
        attachments = attachments,
        parts = parts,
        isGenerating = extraData["generating"] == true,
    )
}

private fun ChatUiState.Message.toTurn(): AIConversationTurn? = when (role) {
    ChatUiState.Message.Role.User -> AIConversationTurn.user(content)
    ChatUiState.Message.Role.Assistant -> AIConversationTurn.assistant(content)
    ChatUiState.Message.Role.Other -> null
}

private fun localMessage(
    role: ChatUiState.Message.Role,
    content: String,
    id: String = UUID.randomUUID().toString(),
    isGenerating: Boolean = false,
) = ChatUiState.Message(
    id = id,
    role = role,
    content = content,
    attachments = emptyList(),
    parts = emptyList(),
    isGenerating = isGenerating,
)

private const val OnDeviceInstructions =
    "You are a helpful assistant answering on the person's device, without a connection to the server. " +
        "Answer briefly. You have no tools."

private fun String.toAssistantState() = when (this) {
    "AI_STATE_THINKING" -> ChatUiState.AssistantState.Thinking
    // The "consulting tools / sources" state has two spellings in the wild: the reference
    // agents send AI_STATE_EXTERNAL_SOURCES, while the backend constant and some SDKs use
    // AI_STATE_CHECKING_SOURCES. ai_state is an unvalidated String, so accept both.
    "AI_STATE_EXTERNAL_SOURCES", "AI_STATE_CHECKING_SOURCES" -> ChatUiState.AssistantState.CheckingSources
    "AI_STATE_GENERATING" -> ChatUiState.AssistantState.Generating
    "AI_STATE_ERROR" -> ChatUiState.AssistantState.Error
    else -> ChatUiState.AssistantState.Idle
}
