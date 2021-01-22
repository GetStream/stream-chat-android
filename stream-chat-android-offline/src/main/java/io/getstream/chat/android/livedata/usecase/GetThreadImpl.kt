package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ThreadController
import io.getstream.chat.android.livedata.utils.validateCid

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
    public operator fun invoke(cid: String, parentId: String): Call<ThreadController>
}

internal class GetThreadImpl(private val domainImpl: ChatDomainImpl) : GetThread {
    override operator fun invoke(cid: String, parentId: String): Call<ThreadController> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        val threadControllerImpl = channelController.getThread(parentId)
        val threadController: ThreadController = threadControllerImpl

        return CoroutineCall(domainImpl.scope) {
            Result(threadController)
        }
    }
}
