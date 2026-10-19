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
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.transformer.ApiModelTransformers
import retrofit2.Retrofit

internal object ParserFactory {
    /**
     * The REST parser, which also reads [ChatEvent]s with the [V2EventParser] the socket and `/sync` use, so event
     * tests run through the production event path.
     */
    fun createMoshiChatParser(
        currentUserIdProvider: () -> String = { "" },
        apiModelTransformers: ApiModelTransformers = ApiModelTransformers(),
        fastEventParsing: Boolean = false,
    ): ChatParser {
        val domainMapping = DomainMapping(
            currentUserIdProvider = currentUserIdProvider,
            channelTransformer = apiModelTransformers.incomingChannelTransformer,
            messageTransformer = apiModelTransformers.incomingMessageTransformer,
            userTransformer = apiModelTransformers.incomingUserTransformer,
            hidesStoredDeclaredCustom = true,
        )
        val directEventParser = DirectEventParser(
            currentUserIdProvider = currentUserIdProvider,
            messageTransformer = apiModelTransformers.incomingMessageTransformer,
            userTransformer = apiModelTransformers.incomingUserTransformer,
            mapping = domainMapping,
        ).takeIf { fastEventParsing }
        return EventRoutingParser(MoshiChatParser(), V2EventParser(domainMapping, directEventParser))
    }

    private class EventRoutingParser(
        private val rest: MoshiChatParser,
        private val events: V2EventParser,
    ) : ChatParser {
        override fun toJson(any: Any): String = rest.toJson(any)

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T = when (clazz) {
            ChatEvent::class.java -> requireNotNull(events.parse(raw)) { "The event parser ignores this frame" } as T
            else -> rest.fromJson(raw, clazz)
        }

        override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder = rest.configRetrofit(builder)
    }
}
