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

package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class MessageListItemMappingTest {

    @Test
    fun `toUiMessageListItem should copy the channel config event flags`() {
        val readEventsEnabled = randomBoolean()
        val deliveryEventsEnabled = randomBoolean()
        val state = MessageItemState(
            message = randomMessage(),
            ownCapabilities = emptySet(),
            readEventsEnabled = readEventsEnabled,
            deliveryEventsEnabled = deliveryEventsEnabled,
        )

        val item = state.toUiMessageListItem() as MessageListItem.MessageItem

        item.readEventsEnabled `should be equal to` readEventsEnabled
        item.deliveryEventsEnabled `should be equal to` deliveryEventsEnabled
    }
}
