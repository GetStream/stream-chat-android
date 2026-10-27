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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.utils.channel.generateChannelIdIfNeeded
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.result.Result

/**
 * [CreateChannelListener] implementation for the state plugin.
 * Records the local placeholder of a channel created without an id, and drops its in-memory state once the server
 * creates the real one.
 *
 * @param logic [LogicRegistry]
 * @param stateRegistry [StateRegistry]
 */
internal class CreateChannelListenerState(
    private val logic: LogicRegistry,
    private val stateRegistry: StateRegistry,
) : CreateChannelListener {

    override suspend fun onCreateChannelRequest(
        channelType: String,
        channelId: String,
        params: CreateChannelParams,
        currentUser: User,
    ) {
        if (channelId.isBlank()) {
            logic.markPlaceholderChannel("$channelType:${generateChannelIdIfNeeded(channelId, params.memberIds)}")
        }
    }

    override suspend fun onCreateChannelResult(
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        result: Result<Channel>,
    ) {
        if (result !is Result.Success) return
        val placeholderCid = "$channelType:${generateChannelIdIfNeeded(channelId, memberIds)}"
        if (result.value.cid != placeholderCid) {
            evictPlaceholderChannel(placeholderCid, logic, stateRegistry, serverChannel = result.value)
        }
    }

    override fun onCreateChannelPrecondition(
        currentUser: User?,
        channelId: String,
        memberIds: List<String>,
    ): Result<Unit> = Result.Success(Unit)
}

/**
 * Drops [placeholderCid] from the channel and query-channels state, so it is no longer synced or watched, and puts
 * [serverChannel] in its place in the channel lists that held it. A consumer already holding its state keeps it as it
 * was.
 */
private suspend fun evictPlaceholderChannel(
    placeholderCid: String,
    logic: LogicRegistry,
    stateRegistry: StateRegistry,
    serverChannel: Channel,
) {
    val (type, id) = placeholderCid.cidToTypeAndId()
    logic.markPlaceholderChannel(placeholderCid)
    logic.removeChannel(type, id)
    stateRegistry.removeChannel(type, id)
    logic.getActiveQueryChannelsLogic()
        .filter { placeholderCid in it.cids() }
        .forEach { queryChannelsLogic ->
            queryChannelsLogic.removeChannel(placeholderCid)
            // An existing distinct channel brings no event that would add it to the list, unless the user hid it
            if (serverChannel.hidden != true) queryChannelsLogic.addChannelFromEvent(serverChannel)
        }
}
