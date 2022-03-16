package io.getstream.chat.android.offline.plugin.internal.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.internal.extensions.toCid
import io.getstream.chat.android.offline.internal.repository.RepositoryFacade
import io.getstream.chat.android.offline.internal.utils.validateCidWithResult
import io.getstream.chat.android.offline.plugin.internal.logic.LogicRegistry
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
