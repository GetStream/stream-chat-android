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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import io.getstream.chat.android.compose.state.messages.attachments.Poll
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelCapabilities
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Test

internal class AttachmentsPickerTabFactoryFilterTest {

    @Test
    fun `Given channel with polls enabled and capabilities to send poll in Normal mode, when filtering attachment factories with polls, poll factory is returned`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = true),
            ownCapabilities = randomChannelCapabilities(
                include = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = true)
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        filteredFactories `should be equal to` factories
    }

    @Test
    fun `Given channel with polls enabled and capabilities to send poll in MessageThread mode, when filtering attachment factories with polls, poll factory is not returned`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = true),
            ownCapabilities = randomChannelCapabilities(
                include = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = true)
        val messageMode = MessageMode.MessageThread(parentMessage = randomMessage())
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        val expected = factories.filterNot { it.attachmentsPickerMode == Poll }
        filteredFactories `should be equal to` expected
    }

    @Test
    fun `Given channel with polls enabled and capabilities to send poll in Normal mode, when filtering attachment factories without polls, poll factory is not returned`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = true),
            ownCapabilities = randomChannelCapabilities(
                include = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = false)
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        val expected = factories.filterNot { it.attachmentsPickerMode == Poll }
        filteredFactories `should be equal to` expected
    }

    @Test
    fun `Given channel with polls enabled without capabilities to send poll in Normal mode, when filtering attachment factories without polls, poll factory is not returned`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = true),
            ownCapabilities = randomChannelCapabilities(
                exclude = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = false)
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        val expected = factories.filterNot { it.attachmentsPickerMode == Poll }
        filteredFactories `should be equal to` expected
    }

    @Test
    fun `Given channel with polls disabled in Normal mode, when filtering attachment factories with polls, poll factory is not returned`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = false),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = true)
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        val expected = factories.filterNot { it.attachmentsPickerMode == Poll }
        filteredFactories `should be equal to` expected
    }

    @Test
    fun `Given channel without capabilities to send poll in Normal mode, when filtering attachment factories with polls, poll factory is not returned`() {
        // given
        val channel = randomChannel(
            ownCapabilities = randomChannelCapabilities(
                exclude = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = true)
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        val expected = factories.filterNot { it.attachmentsPickerMode == Poll }
        filteredFactories `should be equal to` expected
    }

    @Test
    fun `Given channel with polls enabled and capabilities to send poll in Normal mode, when filtering system attachment factories with polls, poll option is enabled`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = true),
            ownCapabilities = randomChannelCapabilities(
                include = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = true,
        )
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        filteredFactories.size `should be equal to` 1
        filteredFactories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val systemFactory = filteredFactories[0] as AttachmentsPickerSystemTabFactory
        systemFactory.config.pollAllowed `should be` true
    }

    @Test
    fun `Given channel with polls enabled and capabilities to send poll in MessageThread mode, when filtering system attachment factories with polls, poll option is disabled`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = true),
            ownCapabilities = randomChannelCapabilities(
                include = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = true,
        )
        val messageMode = MessageMode.MessageThread(parentMessage = randomMessage())
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        filteredFactories.size `should be equal to` 1
        filteredFactories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val systemFactory = filteredFactories[0] as AttachmentsPickerSystemTabFactory
        systemFactory.config.pollAllowed `should be` false
    }

    @Test
    fun `Given channel with polls enabled in Normal mode, when filtering system attachment factories without polls, poll option is disabled`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = true),
            ownCapabilities = randomChannelCapabilities(
                include = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = false,
        )
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        filteredFactories.size `should be equal to` 1
        filteredFactories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val systemFactory = filteredFactories[0] as AttachmentsPickerSystemTabFactory
        systemFactory.config.pollAllowed `should be` false
    }

    @Test
    fun `Given channel with polls disabled in Normal mode, when filtering system attachment factories with polls, poll option is disabled`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = false),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = true,
        )
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        filteredFactories.size `should be equal to` 1
        filteredFactories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val systemFactory = filteredFactories[0] as AttachmentsPickerSystemTabFactory
        systemFactory.config.pollAllowed `should be` false
    }

    @Test
    fun `Given channel with polls enabled without capabilities to send poll in Normal mode, when filtering system attachment factories with polls, poll option is disabled`() {
        // given
        val channel = randomChannel(
            config = Config(pollsEnabled = true),
            ownCapabilities = randomChannelCapabilities(
                exclude = setOf(ChannelCapabilities.SEND_POLL),
            ),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = true,
        )
        val messageMode = MessageMode.Normal
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel, messageMode)
        // then
        filteredFactories.size `should be equal to` 1
        filteredFactories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val systemFactory = filteredFactories[0] as AttachmentsPickerSystemTabFactory
        systemFactory.config.pollAllowed `should be` false
    }
}
