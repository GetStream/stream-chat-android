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

package io.getstream.chat.android.client.internal.state.errorhandler.internal

import io.getstream.chat.android.client.errorhandler.QueryMembersErrorHandler
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.ReturnOnErrorCall
import io.getstream.result.call.onErrorReturn
import kotlinx.coroutines.CoroutineScope

/**
 * [QueryMembersErrorHandler] implementation for
 * [io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin].
 * Checks if the change was done offline and can be synced.
 *
 * @param scope [CoroutineScope]
 * @param clientState [ClientState] provided by the
 * [io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin].
 */
internal class QueryMembersErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val clientState: ClientState,
    private val channelRepository: ChannelRepository,
) : QueryMembersErrorHandler {

    private val logger by taggedLogger("QueryMembersError")

    override fun onQueryMembersError(
        originalCall: Call<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): ReturnOnErrorCall<List<Member>> {
        return originalCall.onErrorReturn(scope) { originalError ->
            logger.d {
                "An error happened while wuery members. " +
                    "Error message: ${originalError.message}. Full error: $originalCall"
            }

            if (clientState.isOnline) {
                Result.Failure(originalError)
            } else {
                // retrieve from database
                val clampedOffset = offset.coerceAtLeast(0)
                val clampedLimit = limit.coerceAtLeast(0)
                val membersFromDatabase = channelRepository
                    .selectMembersForChannel(Pair(channelType, channelId).toCid())
                    .sortedWith(sort.comparator)
                    .drop(clampedOffset)
                    .let { members ->
                        if (clampedLimit > 0) {
                            members.take(clampedLimit)
                        } else {
                            members
                        }
                    }
                Result.Success(membersFromDatabase)
            }
        }
    }
}
