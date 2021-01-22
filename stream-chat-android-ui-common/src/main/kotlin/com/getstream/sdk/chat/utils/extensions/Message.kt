package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain

public fun Message.isMine(): Boolean  {
    return ChatDomain.instance().currentUser.id == this.user.id
}