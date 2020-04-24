package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ThreadController
import java.security.InvalidParameterException

class GetThread(var domainImpl: ChatDomainImpl) {
    operator fun invoke(cid: String, parentId: String): Call2<ThreadController> {
        if (cid.isEmpty()) {
            throw InvalidParameterException("cid cant be empty")
        }

        val channelController = domainImpl.channel(cid)
        val threadControllerImpl = channelController.getThread(parentId)
        val threadController: ThreadController = threadControllerImpl

        val runnable = suspend {
            Result(threadController, null)
        }
        return CallImpl2<ThreadController>(runnable, channelController.scope)
    }
}
