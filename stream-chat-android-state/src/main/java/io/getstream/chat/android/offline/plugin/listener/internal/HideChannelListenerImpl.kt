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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.experimental.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.internal.validateCidWithResult
import io.getstream.chat.android.offline.extensions.internal.toCid
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import java.util.Date

internal class HideChannelListenerImpl(
    private val logic: LogicRegistry,
    private val repositoryFacade: RepositoryFacade,
) : HideChannelListener {

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> = validateCidWithResult(Pair(channelType, channelId).toCid())

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {
        logic.channel(channelType, channelId).setHidden(true)
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        val channelLogic = logic.channel(channelType, channelId)
        if (result.isSuccess) {
            val cid = Pair(channelType, channelId).toCid()
            if (clearHistory) {
                val now = Date()
                channelLogic.run {
                    hideMessagesBefore(now)
                    removeMessagesBefore(now)
                }
                repositoryFacade.deleteChannelMessagesBefore(cid, now)
                repositoryFacade.setHiddenForChannel(cid, true, now)
            } else {
                repositoryFacade.setHiddenForChannel(cid, true)
            }
        } else {
            // Hides the channel if request fails.
            channelLogic.setHidden(false)
        }
    }
}
