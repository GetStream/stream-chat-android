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

package io.getstream.chat.android.ai.compose.sample.data.repository

/**
 * Repository interface for Chat AI operations.
 */
interface ChatAiRepository {
    /**
     * Starts an AI agent for the given channel.
     *
     * @param channelType The channel type (e.g., "messaging")
     * @param channelId The channel ID (e.g., "channel-id")
     * @param platform The AI platform to use ("openai", "anthropic", "gemini", or "xai")
     * @param model Optional model override (e.g., "gpt-4o", "claude-3-5-sonnet-20241022")
     * @return Result containing Unit on success, or an error on failure
     */
    suspend fun startAIAgent(
        channelType: String,
        channelId: String,
        platform: String,
        model: String? = null,
    ): Result<Unit>

    /**
     * Stops the AI agent for the given channel.
     *
     * @param channelId The full identifier for the channel, including type prefix (e.g., "messaging:channel-id")
     * @return Result containing Unit on success, or an error on failure
     */
    suspend fun stopAIAgent(channelId: String): Result<Unit>

    /**
     * Summarizes a text using the specified AI platform.
     *
     * @param text The text to summarize
     * @param platform The AI platform to use ("openai", "anthropic", "gemini", or "xai")
     * @param model Optional model override (e.g., "gpt-4o", "claude-3-5-sonnet-20241022")
     * @return Result containing the summary string on success
     */
    suspend fun summarize(
        text: String,
        platform: String,
        model: String? = null,
    ): Result<String>
}
