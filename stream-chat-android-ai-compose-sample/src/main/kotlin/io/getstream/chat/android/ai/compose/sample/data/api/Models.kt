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

@file:Suppress("ConstructorParameterNaming")

package io.getstream.chat.android.ai.compose.sample.data.api

/**
 * Request model for starting an AI agent.
 *
 * @param channel_type The type of channel (e.g., "messaging")
 * @param channel_id The unique identifier for the channel (without type prefix)
 * @param platform The AI platform to use ("openai", "anthropic", "gemini", or "xai")
 * @param model Optional model override. If null, the platform's default model is used
 */
internal data class StartAIAgentRequest(
    val channel_type: String,
    val channel_id: String,
    val platform: String,
    val model: String?,
)

/**
 * Request model for stopping an AI agent.
 *
 * @param channel_id The full identifier for the channel, including type prefix (e.g., "messaging:channel-id")
 */
internal data class StopAIAgentRequest(
    val channel_id: String,
)

/**
 * Request model for summarizing text.
 *
 * @param text The text to summarize
 * @param platform The AI platform to use ("openai", "anthropic", "gemini", or "xai")
 * @param model Optional model override. If null, the platform's default model is used
 */
internal data class SummarizeRequest(
    val text: String,
    val platform: String,
    val model: String? = null,
)

/**
 * Response model from the Chat AI API.
 *
 * @param message Success message from the server
 * @param data Additional data returned by the server (typically empty)
 */
internal data class AIAgentResponse(
    val message: String,
    val data: List<String> = emptyList(),
)

/**
 * Response model from the summarize API.
 *
 * @param summary The summarized text
 */
internal data class SummarizeResponse(
    val summary: String,
)

/**
 * Error response model for parsing error responses (4xx/5xx status codes).
 *
 * @param error Error message
 * @param reason Additional error reason/details if available
 */
internal data class ErrorResponse(
    val error: String,
    val reason: String? = null,
)
