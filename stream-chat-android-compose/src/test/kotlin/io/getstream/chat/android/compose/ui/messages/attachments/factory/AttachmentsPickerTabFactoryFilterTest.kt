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
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Config
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Test

internal class AttachmentsPickerTabFactoryFilterTest {

    @Test
    fun `Given channel with polls enabled, when filtering attachment factories with polls, poll factory is returned`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = true),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = true)
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel)
        // then
        filteredFactories `should be equal to` factories
    }

    @Test
    fun `Given channel with polls enabled, when filtering attachment factories without polls, poll factory is returned`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = true),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = false)
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel)
        // then
        val expected = factories.filterNot { it.attachmentsPickerMode == Poll }
        filteredFactories `should be equal to` expected
    }

    @Test
    fun `Given channel with polls disabled, when filtering attachment factories with polls, poll factory is not returned`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = false),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactories(pollEnabled = true)
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel)
        // then
        val expected = factories.filterNot { it.attachmentsPickerMode == Poll }
        filteredFactories `should be equal to` expected
    }

    @Test
    fun `Given channel with polls enabled, when filtering system attachment factories with polls, poll option is enabled`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = true),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = true,
        )
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel)
        // then
        filteredFactories.size `should be equal to` 1
        filteredFactories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val systemFactory = filteredFactories[0] as AttachmentsPickerSystemTabFactory
        systemFactory.pollAllowed `should be` true
    }

    @Test
    fun `Given channel with polls enabled, when filtering system attachment factories without polls, poll option is disabled`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = true),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = false,
        )
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel)
        // then
        filteredFactories.size `should be equal to` 1
        filteredFactories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val systemFactory = filteredFactories[0] as AttachmentsPickerSystemTabFactory
        systemFactory.pollAllowed `should be` false
    }

    @Test
    fun `Given channel with polls disabled, when filtering system attachment factories with polls, poll option is disabled`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = false),
        )
        val factories = AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions(
            filesAllowed = true,
            mediaAllowed = true,
            captureImageAllowed = true,
            captureVideoAllowed = true,
            pollAllowed = true,
        )
        // when
        val filter = AttachmentsPickerTabFactoryFilter()
        val filteredFactories = filter.filterAllowedFactories(factories, channel)
        // then
        filteredFactories.size `should be equal to` 1
        filteredFactories[0] `should be instance of` AttachmentsPickerSystemTabFactory::class
        val systemFactory = filteredFactories[0] as AttachmentsPickerSystemTabFactory
        systemFactory.pollAllowed `should be` false
    }
}
