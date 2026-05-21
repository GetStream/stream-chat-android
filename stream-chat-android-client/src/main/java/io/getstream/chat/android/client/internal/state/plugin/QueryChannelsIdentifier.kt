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

package io.getstream.chat.android.client.internal.state.plugin

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Canonical key for query-channels state held in the `LogicRegistry`, `StateRegistry`, and
 * the offline `QueryChannelsRepository`. Each variant represents a distinct way to identify
 * a logical channel-list query.
 */
@InternalStreamChatApi
public sealed interface QueryChannelsIdentifier {

    /**
     * Standard offset-based queryChannels. Identity is `(filter, sort)`.
     */
    public data class Standard(
        public val filter: FilterObject,
        public val sort: QuerySorter<Channel>,
    ) : QueryChannelsIdentifier

    /**
     * Grouped queryChannels. Identity is the stable [groupKey] returned by the server.
     */
    public data class Grouped(
        public val groupKey: String,
    ) : QueryChannelsIdentifier
}

/**
 * Resolves the identifier for a standard [QueryChannelsRequest]. Grouped identifiers are
 * not derivable from a request — they are constructed directly by callers.
 */
@InternalStreamChatApi
public val QueryChannelsRequest.identifier: QueryChannelsIdentifier
    get() = QueryChannelsIdentifier.Standard(filter, querySort)

/**
 * Resolves the identifier for a stored [QueryChannelsSpec]. A non-null [QueryChannelsSpec.groupKey]
 * means this spec was originally produced by a grouped query.
 */
@InternalStreamChatApi
public val QueryChannelsSpec.identifier: QueryChannelsIdentifier
    get() = groupKey?.let { QueryChannelsIdentifier.Grouped(it) }
        ?: QueryChannelsIdentifier.Standard(filter, querySort)
