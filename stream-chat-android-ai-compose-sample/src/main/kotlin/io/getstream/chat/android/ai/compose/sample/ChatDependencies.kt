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

package io.getstream.chat.android.ai.compose.sample

import io.getstream.chat.android.ai.compose.sample.data.api.ChatAiApi
import io.getstream.chat.android.ai.compose.sample.data.repository.ChatAiRepository
import io.getstream.chat.android.ai.compose.sample.data.repository.ChatAiService
import io.getstream.chat.android.ai.compose.sample.di.NetworkModule

/**
 * Holds the dependencies required by the Chat AI Compose components.
 * The wiring is performed in the constructor to keep consumers simple.
 */
public class ChatDependencies(
    baseUrl: String,
    enableLogging: Boolean = true,
    networkModule: NetworkModule = NetworkModule(),
) {

    public val chatAiRepository: ChatAiRepository

    init {
        val moshi = networkModule.createMoshi()
        val okHttpClient = networkModule.createOkHttpClient(enableLogging)
        val retrofit = networkModule.createRetrofit(
            baseUrl = baseUrl,
            okHttpClient = okHttpClient,
            moshi = moshi,
        )
        val chatAiApi = retrofit.create(ChatAiApi::class.java)
        chatAiRepository = ChatAiService(
            chatAiApi = chatAiApi,
            moshi = moshi,
        )
    }
}
