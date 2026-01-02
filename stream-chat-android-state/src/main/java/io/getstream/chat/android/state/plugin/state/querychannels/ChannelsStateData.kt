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

package io.getstream.chat.android.state.plugin.state.querychannels

import io.getstream.chat.android.models.Channel

public sealed class ChannelsStateData {
    /** No query is currently running.
     * If you know that a query will be started you typically want to display a loading icon.
     */
    public object NoQueryActive : ChannelsStateData() {
        override fun toString(): String = "ChannelsStateData.NoQueryActive"
    }

    /** Indicates we are loading the first page of results.
     * We are in this state if QueryChannelsState.loading is true
     * For seeing if we're loading more results have a look at QueryChannelsState.loadingMore
     *
     * @see QueryChannelsState.loadingMore
     * @see QueryChannelsState.loading
     */
    public object Loading : ChannelsStateData() {
        override fun toString(): String = "ChannelsStateData.Loading"
    }

    /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
    public object OfflineNoResults : ChannelsStateData() {
        override fun toString(): String = "ChannelsStateData.OfflineNoResults"
    }

    /** The list of channels, loaded either from offline storage or an API call.
     * Observe chatDomain.online to know if results are currently up to date
     */
    public data class Result(val channels: List<Channel>) : ChannelsStateData() {
        override fun toString(): String {
            return "ChannelsStateData.Result(channels.size=${channels.size})"
        }
    }
}
