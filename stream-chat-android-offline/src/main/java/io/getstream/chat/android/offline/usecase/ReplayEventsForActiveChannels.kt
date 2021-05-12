package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class ReplayEventsForActiveChannels(private val domainImpl: ChatDomainImpl) {
    /**
     * Adds the specified channel to the active channels
     * Replays events for all active channels
     * This ensures that your local storage is up to date with the server
     *
     * @param cid: the full channel id i. e. messaging:123
     */
    @CheckResult
    operator fun invoke(cid: String): Call<List<ChatEvent>> {
        validateCid(cid)

        return CoroutineCall(domainImpl.scope) {
            domainImpl.replayEvents(cid)
        }
    }
}
