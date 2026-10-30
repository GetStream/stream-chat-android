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

package io.getstream.chat.android.client.internal.offline.repository.domain.channelconfig.internal

import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.randomCommand
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.Date

internal class ChannelConfigMapperTest {

    @Test
    fun `ChannelConfig survives the round trip through the database entity`() {
        val channelConfig = ChannelConfig(type = "messaging", config = nonDefaultConfig())

        val result = channelConfig.toEntity().toModel()

        result shouldBeEqualTo channelConfig
    }

    /**
     * Every value differs from its [Config] default, so a column the mappers skip comes back as the default
     * and the comparison notices.
     */
    private fun nonDefaultConfig(): Config = Config(
        createdAt = Date(1000),
        updatedAt = Date(2000),
        name = "messaging",
        typingEventsEnabled = false,
        readEventsEnabled = false,
        deliveryEventsEnabled = false,
        connectEventsEnabled = false,
        searchEnabled = false,
        isReactionsEnabled = false,
        isThreadEnabled = false,
        muteEnabled = false,
        uploadsEnabled = false,
        urlEnrichmentEnabled = false,
        customEventsEnabled = true,
        pushNotificationsEnabled = false,
        skipLastMsgUpdateForSystemMsgs = true,
        pollsEnabled = true,
        messageRetention = "retention",
        maxMessageLength = 500,
        automod = "AI",
        automodBehavior = "flag",
        blocklistBehavior = "block",
        commands = listOf(randomCommand()),
        messageRemindersEnabled = true,
        sharedLocationsEnabled = true,
        markMessagesPending = true,
        pushLevel = "mentions",
        quotesEnabled = false,
        scheduledMessagesMaxPerChannel = 10,
        scheduledMessagesMaxPerUser = 100,
        scheduledMessagesMaxLeadTimeSeconds = 10368000,
        scheduledMessagesMinLeadTimeSeconds = 60,
    )
}
