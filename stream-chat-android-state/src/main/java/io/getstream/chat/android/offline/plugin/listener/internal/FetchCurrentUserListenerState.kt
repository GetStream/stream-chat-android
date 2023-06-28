package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.offline.event.handler.internal.utils.updateCurrentUser
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.event.handler.internal.model.SelfUserFull
import io.getstream.logging.StreamLog

internal class FetchCurrentUserListenerState(
    private val globalMutableState: GlobalMutableState,
) : FetchCurrentUserListener {

    private val logger = StreamLog.getLogger("Chat:FetchCurUserLstnr")

    override fun onFetchCurrentUserResult(result: Result<User>) {
        if (result.isSuccess) {
            logger.d { "[onFetchCurrentUserResult] result: $result" }
            globalMutableState.updateCurrentUser(SelfUserFull(result.data()))
        }
    }
}