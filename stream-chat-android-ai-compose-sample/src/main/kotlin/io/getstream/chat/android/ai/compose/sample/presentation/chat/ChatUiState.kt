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

import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.models.Attachment

/**
 * Represents the UI state for a chat conversation.
 *
 * @param isLoading Whether the chat is currently loading initial data
 * @param title The title of the chat conversation
 * @param actions Available actions for the chat (e.g., NewChat, DeleteChat)
 * @param messages List of messages in the conversation, ordered from newest to oldest
 * @param assistantState The current state of the AI assistant
 */
data class ChatUiState(
    val isLoading: Boolean = false,
    val title: String = "New Chat",
    val actions: List<Action> = emptyList(),
    val messages: List<Message> = emptyList(),
    val assistantState: AssistantState = AssistantState.Idle,
) {
    /**
     * Available actions that can be performed on the chat.
     */
    enum class Action {
        /** Action to start a new chat conversation. */
        NewChat,

        /** Action to delete the current chat conversation. */
        DeleteChat,
    }

    /**
     * Represents a message in the chat conversation.
     *
     * @param id Unique identifier for the message
     * @param role The role of the message sender (Assistant, User, or Other)
     * @param content The text content of the message
     * @param attachments List of attachments associated with the message
     * @param parts The AI reply's steps, such as its reasoning and tool calls
     * @param isGenerating Indicates if the message is currently being generated
     */
    data class Message(
        val id: String,
        val role: Role,
        val content: String,
        val attachments: List<Attachment>,
        val parts: List<AIMessagePart>,
        val isGenerating: Boolean,
    ) {
        /**
         * Represents the role of a message sender in the conversation.
         */
        sealed class Role {
            /** Message from the AI assistant. */
            data object Assistant : Role()

            /** Message from the current user. */
            data object User : Role()

            /** Message from another user. */
            data object Other : Role()
        }
    }

    /**
     * Represents the current state of the AI assistant.
     */
    enum class AssistantState {
        /** Assistant is idle and ready. */
        Idle,

        /** Assistant is thinking/processing. */
        Thinking,

        /** Assistant is checking sources. */
        CheckingSources,

        /** Assistant is generating a response. */
        Generating,

        /** Assistant encountered an error. */
        Error,
    }
}

/**
 * Checks if the assistant is currently busy (not idle and not in error state).
 *
 * @return true if the assistant is actively working, false otherwise
 */
fun ChatUiState.AssistantState.isBusy(): Boolean =
    this != ChatUiState.AssistantState.Idle && this != ChatUiState.AssistantState.Error

/**
 * Gets the most recent assistant message from the conversation.
 *
 * @return The latest assistant message, or null if there are no assistant messages
 */
fun ChatUiState.getCurrentAssistantMessage(): ChatUiState.Message? =
    messages.firstOrNull()?.takeIf { message -> message.role == ChatUiState.Message.Role.Assistant }

/**
 * Gets the most recent user message from the conversation.
 *
 * @return The latest user message, or null if there are no user messages
 */
fun ChatUiState.getCurrentUserMessage(): ChatUiState.Message? =
    messages.firstOrNull()?.takeIf { message -> message.role == ChatUiState.Message.Role.User }
