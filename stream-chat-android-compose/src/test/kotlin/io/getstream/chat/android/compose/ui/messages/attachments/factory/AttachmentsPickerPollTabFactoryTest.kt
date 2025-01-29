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

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Config
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

internal class AttachmentsPickerPollTabFactoryTest {

    @Test
    fun `Given Channel with disabled poll, Then picker tab is disabled`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = false),
            ownCapabilities = setOf(ChannelCapabilities.SEND_POLL),
        )
        // when
        val factory = AttachmentsPickerPollTabFactory()
        val isTabEnabled = factory.isPickerTabEnabled(channel)
        // then
        isTabEnabled shouldBe false
    }

    @Test
    fun `Given Channel with enabled poll and no send poll capability, Then picker tab is disabled`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = false),
            ownCapabilities = emptySet(),
        )
        // when
        val factory = AttachmentsPickerPollTabFactory()
        val isTabEnabled = factory.isPickerTabEnabled(channel)
        // then
        isTabEnabled shouldBe false
    }

    @Test
    fun `Given Channel with enabled poll and send poll capability, Then picker tab is enabled`() {
        // given
        val channel = Channel(
            id = "cid1",
            config = Config(pollsEnabled = true),
            ownCapabilities = setOf(ChannelCapabilities.SEND_POLL),
        )
        // when
        val factory = AttachmentsPickerPollTabFactory()
        val isTabEnabled = factory.isPickerTabEnabled(channel)
        // then
        isTabEnabled shouldBe true
    }
}
