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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.mock

/** A real [ChannelStateImpl] driven by a real [ChannelLogicImpl], for tests that check the resulting state. */
internal class ChannelLogicImplWithRealState(
    channelType: String,
    channelId: String,
    currentUserId: String,
    scope: CoroutineScope,
) {
    val globalState = MutableGlobalState(currentUserId)

    val state = ChannelStateImpl(
        channelType = channelType,
        channelId = channelId,
        currentUser = MutableStateFlow(User(id = currentUserId)),
        latestUsers = MutableStateFlow(emptyMap()),
        mutedUsers = MutableStateFlow(emptyList()),
        liveLocations = globalState.activeLiveLocations,
        messageLimit = null,
    )

    val logic = ChannelLogicImpl(
        cid = state.cid,
        messagesUpdateLogic = mock(),
        repository = mock(),
        state = state,
        mutableGlobalState = globalState,
        userPresence = true,
        coroutineScope = scope,
        getCurrentUserId = { currentUserId },
        now = { System.currentTimeMillis() },
    )
}
