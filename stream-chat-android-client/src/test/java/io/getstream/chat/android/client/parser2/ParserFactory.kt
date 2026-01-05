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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.transformer.ApiModelTransformers

internal object ParserFactory {
    fun createMoshiChatParser(
        currentUserIdProvider: () -> String = { "" },
        apiModelTransformers: ApiModelTransformers = ApiModelTransformers(),
    ): MoshiChatParser = MoshiChatParser(
        eventMapping = EventMapping(
            DomainMapping(
                currentUserIdProvider = currentUserIdProvider,
                channelTransformer = apiModelTransformers.incomingChannelTransformer,
                messageTransformer = apiModelTransformers.incomingMessageTransformer,
                userTransformer = apiModelTransformers.incomingUserTransformer,
            ),
        ),
        dtoMapping = DtoMapping(
            messageTransformer = apiModelTransformers.outgoingMessageTransformer,
            userTransformer = apiModelTransformers.outgoingUserTransformers,
        ),
    )
}
