package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * @return if the message has been deleted.
 */
@InternalStreamChatApi
internal fun Message.isDeleted(): Boolean = deletedAt != null