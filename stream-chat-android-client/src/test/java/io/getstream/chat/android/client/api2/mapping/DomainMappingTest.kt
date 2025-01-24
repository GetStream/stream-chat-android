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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.Mother.randomDownstreamChannelDto
import io.getstream.chat.android.client.Mother.randomDownstreamMessageDto
import io.getstream.chat.android.client.Mother.randomDownstreamUserDto
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class DomainMappingTest {

    @Test
    fun `Message should be transformed after it is mapped`() {
        val transformedMessage = randomMessage()
        val messageTransformer = object : MessageTransformer {
            override fun transform(message: Message): Message = transformedMessage
        }

        val sut = Fixture()
            .withMessageTransformer(messageTransformer)
            .get()

        val result = with(sut) {
            randomDownstreamMessageDto().toDomain()
        }

        result `should be equal to` transformedMessage
    }

    @Test
    fun `User should be transformed after it is mapped`() {
        val transformedUser = randomUser()
        val userTransformer = object : UserTransformer {
            override fun transform(user: User): User = transformedUser
        }

        val sut = Fixture()
            .withUserTransformer(userTransformer)
            .get()

        val result = with(sut) {
            randomDownstreamUserDto().toDomain()
        }

        result `should be equal to` transformedUser
    }

    @Test
    fun `Channel should be transformed after it is mapped`() {
        val transformedChannel = randomChannel()
        val channelTransformer = object : ChannelTransformer {
            override fun transform(channel: Channel): Channel = transformedChannel
        }

        val sut = Fixture()
            .withChannelTransformer(channelTransformer)
            .get()

        val result = with(sut) {
            randomDownstreamChannelDto().toDomain(randomDateOrNull())
        }

        result `should be equal to` transformedChannel
    }

    internal class Fixture {
        private var currentUserIdProvider: () -> UserId? = { randomString() }
        private var channelTransformer: ChannelTransformer = NoOpChannelTransformer
        private var messageTransformer: MessageTransformer = NoOpMessageTransformer
        private var userTransformer: UserTransformer = NoOpUserTransformer

        fun withCurrentUserIdProvider(provider: () -> UserId?): Fixture = apply {
            currentUserIdProvider = provider
        }

        fun withChannelTransformer(transformer: ChannelTransformer): Fixture = apply {
            channelTransformer = transformer
        }

        fun withMessageTransformer(transformer: MessageTransformer): Fixture = apply {
            messageTransformer = transformer
        }

        fun withUserTransformer(transformer: UserTransformer): Fixture = apply {
            userTransformer = transformer
        }

        fun get(): DomainMapping {
            return DomainMapping(
                currentUserIdProvider = currentUserIdProvider,
                channelTransformer = channelTransformer,
                messageTransformer = messageTransformer,
                userTransformer = userTransformer,
            )
        }
    }
}
