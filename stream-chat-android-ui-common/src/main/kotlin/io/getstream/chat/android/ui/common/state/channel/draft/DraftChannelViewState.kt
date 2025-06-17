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

package io.getstream.chat.android.ui.common.state.channel.draft

import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Channel

/**
 * Represents the state of the draft channel in the UI.
 *
 * This interface defines the possible states of the draft channel view,
 * including loading and content states.
 */
@ExperimentalStreamChatApi
public sealed interface DraftChannelViewState {

    /**
     * Represents the loading state of the draft channel.
     */
    public data object Loading : DraftChannelViewState

    /**
     * Represents the content state of the draft channel.
     *
     * @param channel The [Channel] object representing the direct channel being previewed.
     */
    public data class Content(val channel: Channel) : DraftChannelViewState
}
