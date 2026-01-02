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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Represents of possible state of messages for [ChannelState].
 */
@Immutable
public sealed class MessagesState {
    /**
     * The ChannelState is initialized but no query is currently running.
     * If you know that a query will be started you typically want to display a loading icon.
     */
    @Immutable
    public data object NoQueryActive : MessagesState() {
        override fun toString(): String = "NoQueryActive"
    }

    /**
     * Indicates we are loading the first page of results.
     * We are in this state if ChannelState.loading is true
     * For seeing if we're loading more results have a look at loadingNewerMessages and loadingOlderMessages
     *
     * @see loading
     * @see loadingNewerMessages
     * @see loadingOlderMessages
     */
    @Immutable
    public data object Loading : MessagesState() {
        override fun toString(): String = "Loading"
    }

    /** If we are offline and don't have channels stored in offline storage, typically displayed as an error
     * condition. */
    @Immutable
    public data object OfflineNoResults : MessagesState() {
        override fun toString(): String = "OfflineNoResults"
    }

    /**
     * The list of messages, loaded either from offline storage or an API call.
     * Observe chatDomain.online to know if results are currently up to date
     *
     * @param messages Message collection of this channel.
     */
    @Immutable
    public data class Result(val messages: List<Message>) : MessagesState()
}
