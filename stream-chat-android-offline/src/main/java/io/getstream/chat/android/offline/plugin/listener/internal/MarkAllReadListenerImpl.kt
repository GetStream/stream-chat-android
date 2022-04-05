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

import io.getstream.chat.android.client.experimental.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.utils.internal.ChannelMarkReadHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * [MarkAllReadListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Marks all active channels as read if needed.
 *
 * @param logic [LogicRegistry]
 * @param scope [CoroutineScope]]
 * @param channelMarkReadHelper [ChannelMarkReadHelper]
 */
internal class MarkAllReadListenerImpl(
    private val logic: LogicRegistry,
    private val scope: CoroutineScope,
    private val channelMarkReadHelper: ChannelMarkReadHelper,
) : MarkAllReadListener {

    /**
     * Marks all active channels as read if needed.
     *
     * @see [ChannelMarkReadHelper.markChannelReadLocallyIfNeeded]]
     * @see [LogicRegistry.getActiveChannelsLogic]
     */
    override suspend fun onMarkAllReadRequest() {
        logic.getActiveChannelsLogic().map { channel ->
            scope.async(DispatcherProvider.Main) {
                val (channelType, channelId) = channel.cid.cidToTypeAndId()
                channelMarkReadHelper.markChannelReadLocallyIfNeeded(channelType = channelType, channelId = channelId)
            }
        }.awaitAll()
    }
}
