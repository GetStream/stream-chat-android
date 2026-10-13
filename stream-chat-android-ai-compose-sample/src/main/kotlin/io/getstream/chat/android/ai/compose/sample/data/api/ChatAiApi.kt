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

package io.getstream.chat.android.ai.compose.sample.data.api

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for Chat AI endpoints.
 * These endpoints communicate with the backend server to manage AI agents for chat channels.
 */
internal interface ChatAiApi {
    /**
     * Starts an AI agent for a specific channel.
     */
    @POST("/start-ai-agent")
    suspend fun startAIAgent(@Body request: StartAIAgentRequest): AIAgentResponse

    /**
     * Stops the AI agent for a specific channel.
     */
    @POST("/stop-ai-agent")
    suspend fun stopAIAgent(@Body request: StopAIAgentRequest): AIAgentResponse

    /**
     * Summarizes text using the specified AI platform.
     */
    @POST("/summarize")
    suspend fun summarize(@Body request: SummarizeRequest): SummarizeResponse
}
