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

package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.interceptor.SendMessageInterceptor
import io.getstream.chat.android.models.Message
import io.getstream.result.call.Call
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.CoroutineScope

/**
 * Serves as a proxy for the [ChatApi] interface, allowing for the interception of API calls.
 *
 * @param delegate The original [ChatApi] instance to delegate calls to.
 * @param scope The [CoroutineScope] used for executing the API calls.
 * @param sendMessageInterceptor An optional [SendMessageInterceptor] for intercepting and overriding the 'sendMessage'
 * API call.
 */
internal class ProxyChatApi(
    private val delegate: ChatApi,
    private val scope: CoroutineScope,
    private val sendMessageInterceptor: SendMessageInterceptor?,
) : ChatApi by delegate {

    override fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message> {
        return sendMessageInterceptor?.let {
            CoroutineCall(scope) {
                it.sendMessage(channelType, channelId, message)
            }
        } ?: delegate.sendMessage(channelType, channelId, message)
    }
}
