/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.sender

import io.getstream.chat.android.client.api2.endpoint.MessageApi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.model.requests.SendMessageRequest
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/**
 * Default implementation of [MessageSender].
 * Handles the sending of [Message]s to the Stream server.
 *
 * @param api The [MessageApi] instance used to send messages.
 * @param dtoMapping The [DtoMapping] instance used to map domain models to DTOs.
 * @param domainMapping The [DomainMapping] instance used to map DTOs to domain models.
 */
@OptIn(ExperimentalStreamChatApi::class)
internal class StreamMessageSender(
    private val api: MessageApi,
    private val dtoMapping: DtoMapping,
    private val domainMapping: DomainMapping,
) : MessageSender {
    override fun sendMessage(channelType: String, channelId: String, message: Message): Result<Message> {
        return api.sendMessage(
            channelType = channelType,
            channelId = channelId,
            message = SendMessageRequest(
                message = with(dtoMapping) { message.toDto() },
                skip_push = message.skipPushNotification,
                skip_enrich_url = message.skipEnrichUrl,
            ),
        ).execute().map {
            with(domainMapping) { it.message.toDomain() }
        }
    }
}
