package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result

public interface FetchCurrentUserListener {

    public fun onFetchCurrentUserResult(result: Result<User>)

}