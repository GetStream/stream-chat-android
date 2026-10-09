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

import com.squareup.moshi.Moshi
import io.getstream.chat.android.ai.compose.sample.data.api.ChatAiApi
import io.getstream.chat.android.ai.compose.sample.data.api.ErrorResponse
import io.getstream.chat.android.ai.compose.sample.data.api.StartAIAgentRequest
import io.getstream.chat.android.ai.compose.sample.data.api.StopAIAgentRequest
import io.getstream.chat.android.ai.compose.sample.data.api.SummarizeRequest
import retrofit2.HttpException

/**
 * Service implementation for communicating with the Chat AI server.
 */
internal class ChatAiService(
    private val chatAiApi: ChatAiApi,
    private val moshi: Moshi,
) : ChatAiRepository {

    override suspend fun startAIAgent(
        channelType: String,
        channelId: String,
        platform: String,
        model: String?,
    ): Result<Unit> = executeApiCall {
        chatAiApi.startAIAgent(
            request = StartAIAgentRequest(
                channel_type = channelType,
                channel_id = channelId,
                platform = platform,
                model = model,
            ),
        )
    }

    override suspend fun stopAIAgent(
        channelId: String,
    ): Result<Unit> = executeApiCall {
        chatAiApi.stopAIAgent(
            request = StopAIAgentRequest(channel_id = channelId),
        )
    }

    override suspend fun summarize(
        text: String,
        platform: String,
        model: String?,
    ): Result<String> = executeApiCall {
        chatAiApi.summarize(
            request = SummarizeRequest(
                text = text,
                platform = platform,
                model = model,
            ),
        ).summary
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T> executeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: HttpException) {
            handleHttpException(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> handleHttpException(e: HttpException): Result<T> {
        val errorMessage = try {
            e.fromErrorResponse() ?: e.toHttpString()
        } catch (_: Exception) {
            e.toHttpString()
        }
        return Result.failure(RuntimeException(errorMessage))
    }

    private fun HttpException.fromErrorResponse(): String? {
        val errorBody = response()?.errorBody()?.string() ?: return null
        val errorResponse = moshi.adapter(ErrorResponse::class.java).fromJson(errorBody) ?: return null
        val reason = errorResponse.reason?.let { ": $it" }.orEmpty()
        return errorResponse.error + reason
    }
}

private fun HttpException.toHttpString(): String = "HTTP ${code()}: ${message()}"
