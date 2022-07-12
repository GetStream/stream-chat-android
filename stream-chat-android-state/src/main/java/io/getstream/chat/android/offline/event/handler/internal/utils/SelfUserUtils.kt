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

package io.getstream.chat.android.offline.event.handler.internal.utils

import io.getstream.chat.android.client.setup.state.ClientMutableState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.mergePartially
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUser
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUserFull
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUserPart
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState

/**
 * Updates [MutableGlobalState] with [SelfUser] instance.
 */
internal fun MutableGlobalState.updateCurrentUser(self: SelfUser) {
    val me = when (self) {
        is SelfUserFull -> self.me
        is SelfUserPart -> user.value?.mergePartially(self.me) ?: self.me
    }

    clientState.toMutableState()?.setUser(me)
    setBanned(me.banned)
    setBanned(me.banned)
    setMutedUsers(me.mutes)
    setChannelMutes(me.channelMutes)
    setTotalUnreadCount(me.totalUnreadCount)
    setChannelUnreadCount(me.unreadChannels)
}

private fun ClientState.toMutableState(): ClientMutableState? = this as? ClientMutableState
