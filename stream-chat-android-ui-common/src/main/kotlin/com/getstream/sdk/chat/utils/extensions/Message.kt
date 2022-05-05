package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message

/**
 * TODO
 */
public fun Message.isMine(): Boolean = ChatClient.instance().getCurrentUser()?.id == user.id