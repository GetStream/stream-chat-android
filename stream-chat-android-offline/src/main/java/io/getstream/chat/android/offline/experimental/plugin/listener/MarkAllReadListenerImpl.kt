package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.channel.ChannelMarkReadHelper
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * [MarkAllReadListener] implementation for [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
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
