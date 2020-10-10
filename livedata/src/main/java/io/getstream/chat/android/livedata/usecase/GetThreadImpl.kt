package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ThreadController
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

interface GetThread {
    /**
     * Returns a thread controller for the given channel and message id
     *
     * @param cid the full channel id. ie messaging:123
     * @param parentId the message id for the parent of this thread

     * @return A call object with ThreadController as the return type
     * @see io.getstream.chat.android.livedata.controller.ThreadController
     */
    operator fun invoke(cid: String, parentId: String): Call2<ThreadController>
}

class GetThreadImpl(var domainImpl: ChatDomainImpl) : GetThread {
    override operator fun invoke(cid: String, parentId: String): Call2<ThreadController> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        val threadControllerImpl = channelController.getThread(parentId)
        val threadController: ThreadController = threadControllerImpl

        val runnable = suspend {
            Result(threadController, null)
        }
        return CallImpl2(
            runnable,
            channelController.scope
        )
    }
}
