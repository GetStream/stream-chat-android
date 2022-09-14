/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
internal interface QueryChannelsStateLogic {

    fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult

    fun isLoading(): Boolean

    fun getChannelsOffset(): Int

    fun getChannels(): Map<String, Channel>?

    fun getQuerySpecs(): QueryChannelsSpec

    fun getState(): QueryChannelsState

    fun setLoading(isLoading: Boolean)

    fun setCurrentRequest(request: QueryChannelsRequest)

    fun setEndOfChannels(isEnd: Boolean)

    fun setRecoveryNeeded(recoveryNeeded: Boolean)

    fun setChannelsOffset(offset: Int)

    fun incrementChannelsOffset(size: Int)

    fun loadingForCurrentRequest(): MutableStateFlow<Boolean>

    fun addChannelsState(channels: List<Channel>)

    fun removeChannels(cidSet: Set<String>)

    fun initializeChannelsIfNeeded()

    fun refreshChannels(cidList: Collection<String>)

    fun refreshMembersStateForUser(newUser: User)
}
