package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.utils.validateCid
import io.getstream.chat.android.offline.ChatDomainImpl as NewChatDomainImpl
import io.getstream.chat.android.offline.ThreadController as NewThreadController

public interface GetThread {
    /**
     * Returns a thread controller for the given channel and message id
     *
     * @param cid the full channel id. ie messaging:123
     * @param parentId the message id for the parent of this thread
     *
     * @see io.getstream.chat.android.livedata.controller.ThreadController
     */
    @CheckResult
    public operator fun invoke(cid: String, parentId: String): Call<NewThreadController>
}

internal class GetThreadImpl(private val domainImpl: NewChatDomainImpl) : GetThread {
    override operator fun invoke(cid: String, parentId: String): Call<NewThreadController> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        val threadControllerImpl = channelController.getThread(parentId)
        val threadController: NewThreadController = threadControllerImpl

        return CoroutineCall(domainImpl.scope) {
            Result(threadController)
        }
    }
}
