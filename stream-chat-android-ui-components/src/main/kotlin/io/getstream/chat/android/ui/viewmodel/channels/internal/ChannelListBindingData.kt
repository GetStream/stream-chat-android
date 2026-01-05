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

package io.getstream.chat.android.ui.viewmodel.channels.internal

import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel

internal data class ChannelListBindingData(
    val state: ChannelListViewModel.State = ChannelListViewModel.State(false, emptyList()),
    val paginationState: ChannelListViewModel.PaginationState = ChannelListViewModel.PaginationState(),
    val typingEvents: Map<String, TypingEvent> = emptyMap(),
    val draftMessages: Map<String, DraftMessage> = emptyMap(),
)
