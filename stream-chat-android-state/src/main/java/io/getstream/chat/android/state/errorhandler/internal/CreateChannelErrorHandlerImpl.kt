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

package io.getstream.chat.android.state.errorhandler.internal

import io.getstream.chat.android.client.errorhandler.CreateChannelErrorHandler
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.channel.generateChannelIdIfNeeded
import io.getstream.chat.android.models.Channel
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.ReturnOnErrorCall
import io.getstream.result.call.onErrorReturn
import kotlinx.coroutines.CoroutineScope

/**
 * [CreateChannelErrorHandler] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Returns a [Channel] instance if the channel was created offline and can be synced.
 *
 * @param scope [CoroutineScope]
 * @param channelRepository [ChannelRepository]
 * @param clientState [ClientState]
 */
internal class CreateChannelErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val channelRepository: ChannelRepository,
    private val clientState: ClientState,
) : CreateChannelErrorHandler {

    /**
     * Replaces the original response error if the user is offline and the channel exists in the cache.
     * This means that the channel was created locally but the API request failed due to lack of connection.
     * The request will be synced once user's connection is recovered.
     *
     * @param originalCall The original call.
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     * @param extraData Map of key-value pairs that let you store extra data
     *
     * @return result The original or offline related result.
     */
    override fun onCreateChannelError(
        originalCall: Call<Channel>,
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
    ): ReturnOnErrorCall<Channel> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (clientState.isOnline) {
                Result.Failure(originalError)
            } else {
                val generatedCid =
                    "$channelType:${generateChannelIdIfNeeded(channelId = channelId, memberIds = memberIds)}"
                val cachedChannel = channelRepository.selectChannels(listOf(generatedCid)).firstOrNull()
                if (cachedChannel == null) {
                    Result.Failure(Error.GenericError(message = "Channel wasn't cached properly."))
                } else {
                    Result.Success(cachedChannel)
                }
            }
        }
    }
}
