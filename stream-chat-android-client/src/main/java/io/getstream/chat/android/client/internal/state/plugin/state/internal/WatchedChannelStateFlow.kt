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

package io.getstream.chat.android.client.internal.state.plugin.state.internal

import io.getstream.chat.android.client.channel.state.ChannelState
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

/**
 * A [StateFlow] wrapper returned by [io.getstream.chat.android.client.internal.state.extensions.watchChannelAsState].
 * Identifies a watched channel by [cid]; used as a weak key in
 * [io.getstream.chat.android.client.api.state.StateRegistry]'s tracker so the entry is evicted
 * automatically once the caller releases the returned flow.
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
internal class WatchedChannelStateFlow(
    private val delegate: StateFlow<ChannelState?>,
    val cid: String,
) : StateFlow<ChannelState?> by delegate
