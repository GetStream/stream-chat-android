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

package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.channel.state.ChannelStateLogicProvider
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelStateImpl

/**
 * Implementation of [ChannelMessagesUpdateLogic] backed by [ChannelStateImpl].
 * Serves as the bridge between the LLC and the channel state for message updates. It is provided via the
 * [ChannelStateLogicProvider] to the LLC, when using the non-legacy state management plugin.
 *
 * @param state The [ChannelStateImpl] instance to interact with for state updates.
 *
 * @see ChannelMessagesUpdateLogic
 * @see ChannelStateLogicProvider
 */
internal class ChannelMessagesUpdateLogicImpl(private val state: ChannelStateImpl) : ChannelMessagesUpdateLogic {

    override fun upsertMessage(message: Message) {
        state.upsertMessage(message)
    }

    override fun channelState(): ChannelState {
        return state
    }

    override fun setRepliedMessage(repliedMessage: Message?) {
        state.setRepliedMessage(repliedMessage)
    }
}
